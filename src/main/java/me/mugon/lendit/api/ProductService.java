package me.mugon.lendit.api;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.common.BaseValidator;
import me.mugon.lendit.domain.product.Product;
import me.mugon.lendit.domain.product.ProductRepository;
import me.mugon.lendit.domain.product.ProductResource;
import me.mugon.lendit.web.OrdersController;
import me.mugon.lendit.web.ProductController;
import me.mugon.lendit.web.dto.product.ProductRequestDto;
import me.mugon.lendit.web.dto.product.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static me.mugon.lendit.api.error.ErrorMessageConstant.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final BaseValidator baseValidator;

    /**
     * 상품 생성
     * 1. 클라이언트에게 전달받은 Dto를 Entity Class로 매핑
     * 2. 상품을 등록한 유저와 Entity Class를 매핑
     * 3. db에 저장
     * 4. HATEOAS를 위해 query-products, self, update-product, delete-product, order 관계를 EntityModel에 더함
     * 5. Self Descriptive Message를 위해 API Guide의 주소를 profile 관계로 명시하여 더함
     * 6. Header의 Location옵션에 생성된 상품을 조회할 수 있는 링크를 담고, Body에 위의 EntityModel을 실어 반환
     */
    @Transactional
    public ResponseEntity<?> registrationProduct(ProductRequestDto productRequestDto, Account currentUser) {
        Product product = productRequestDto.toEntity(currentUser);
        product.mapUser(currentUser);
        Product savedProduct = productRepository.save(product);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(ProductController.class).slash(savedProduct.getId());
        ProductResponseDto responseDto = new ProductResponseDto(savedProduct);
        ProductResource productResource = new ProductResource(responseDto);
        productResource.add(linkTo(ProductController.class).withRel("query-products"));
        productResource.add(linkTo(ProductController.class).slash(responseDto.getId()).withRel("update-product"));
        productResource.add(linkTo(ProductController.class).slash(responseDto.getId()).withRel("delete-product"));
        productResource.add(linkTo(OrdersController.class).withRel("order"));
        productResource.add(new Link("https://mkshin96.github.io/Coding-Task/#resources-products-create").withRel("profile"));
        return ResponseEntity.created(selfLinkBuilder.toUri()).body(productResource);
    }

    /**
     * 상품 수정
     * 1. url경로로 전달받은 상품의 id로 db를 검색
     * 2. db에 없다면 Body에 'message: 상품을 찾을 수 없습니다.' 를 실어서 Bad Request와 함께 반환
     * 3. db에 있다면 전달받은 dto의 값으로 데이터 변경
     * 4. HATEOAS를 위해 delete-product, self, query-products, create-product 관계를 EntityModel에 더함
     * 5. Self Descriptive Message를 위해 API Guide의 주소를 profile 관계로 명시하여 더함
     * 6. Body에 위의 EntityModel을 실어 반환
     */
    @Transactional
    public ResponseEntity<?> updateProduct(Long productId, ProductRequestDto productRequestDto, Account currentUser) {
        Optional<Product> optionalProduct = findById(productId);
        if (!optionalProduct.isPresent()) { //수정하려는 상품이 데이터베이스에 있는 상품인지 확인
            return new ResponseEntity<>(baseValidator.returnErrorMessage(PRODUCTNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        Product product = optionalProduct.get();
        if (!product.getAccount().getId().equals(currentUser.getId())) { //요청한 사용자가 상품을 등록한 사용자인지 확인
            return new ResponseEntity<>(baseValidator.returnErrorMessage(INVALIDUSER), HttpStatus.BAD_REQUEST);
        }
        product.update(productRequestDto);
        ProductResponseDto responseDto = new ProductResponseDto(product);
        ProductResource productResource = new ProductResource(responseDto);
        productResource.add(linkTo(ProductController.class).withRel("query-products"));
        productResource.add(linkTo(ProductController.class).slash(responseDto.getId()).withRel("delete-product"));
        productResource.add(linkTo(ProductController.class).withRel("create-product"));
        productResource.add(new Link("https://mkshin96.github.io/Coding-Task/#resources-products-update").withRel("profile"));
        return new ResponseEntity<>(productResource, HttpStatus.OK);
    }

    /**
     * 상품 삭제
     * 1. url경로로 전달받은 상품의 id로 db를 검색
     * 2. db에 없다면 Body에 'message: 상품을 찾을 수 없습니다.' 를 실어서 Bad Request와 함께 반환
     * 3. db에 있다면 삭제
     * 4. HATEOAS를 위해 create-product, self, query-products 관계를 EntityModel에 더함
     * 5. Self Descriptive Message를 위해 API Guide의 주소를 profile 관계로 명시하여 더함
     * 6. Body에 위의 EntityModel을 실어 반환
     */
    @Transactional
    public ResponseEntity<?> deleteProduct(Long productId, Account currentUser) {
        Optional<Product> optionalProduct = findById(productId);
        if (!optionalProduct.isPresent()) { //상품이 저장되어있는지 확인
            return new ResponseEntity<>(baseValidator.returnErrorMessage(PRODUCTNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        Product product = optionalProduct.get();
        if (!product.getAccount().getId().equals(currentUser.getId())) { //상품을 등록한 유저가 맞는지 확인
            return new ResponseEntity<>(baseValidator.returnErrorMessage(INVALIDUSER), HttpStatus.BAD_REQUEST);
        }
        productRepository.delete(product);
        ProductResource productResource = new ProductResource(new ProductResponseDto(product));
        productResource.add(linkTo(ProductController.class).withRel("create-product"));
        productResource.add(linkTo(ProductController.class).withRel("query-products"));
        productResource.add(new Link("https://mkshin96.github.io/Coding-Task/#resources-products-delete").withRel("profile"));
        return new ResponseEntity<>(productResource, HttpStatus.OK);
    }

    /**
     * 상품 리스트 조회
     * 1. db의 모든 상품을 Paging을 거쳐 조회
     * 2. HATEOAS를 위해 self 관계를 EntityModel에 더함
     * 3. Self Descriptive Message를 위해 API Guide의 주소를 profile 관계로 명시하여 더함
     * 4. Body에 위의 EntityModel을 실어 반환
     */
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProductList(Pageable pageable, PagedResourcesAssembler<Product> assembler) {
        Page<Product> all = productRepository.findAll(pageable);
        PagedModel<ProductResource> productResources = assembler.toModel(all, e -> new ProductResource(new ProductResponseDto(e)));
        productResources.add(new Link("https://mkshin96.github.io/Coding-Task/#resources-products-list").withRel("profile"));
        return ResponseEntity.ok(productResources);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getProduct(Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (!optionalProduct.isPresent()) {
            return new ResponseEntity<>(baseValidator.returnErrorMessage(PRODUCTNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        ProductResource productResource = new ProductResource(new ProductResponseDto(optionalProduct.get()));
        productResource.add(linkTo(OrdersController.class).withRel("order"));
        productResource.add(linkTo(ProductController.class).withRel("query-products"));
        productResource.add(new Link("https://mkshin96.github.io/Coding-Task/#resources-get-product").withRel("profile"));
        return new ResponseEntity<>(productResource, HttpStatus.OK);
    }

    Optional<Product> findById(Long productId) {
        return productRepository.findById(productId);
    }
}
