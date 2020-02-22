package me.mugon.lendit.api;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.domain.account.Account;
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
        return ResponseEntity.created(selfLinkBuilder.toUri()).body(productResource);
    }

    @Transactional
    public ResponseEntity<?> updateProduct(Long productId, ProductRequestDto productRequestDto, Account currentUser) {
        Optional<Product> optionalProduct = findById(productId);
        if (!optionalProduct.isPresent()) { //수정하려는 상품이 데이터베이스에 있는 상품인지
            return new ResponseEntity<>(getErrorMap(PRODUCTNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        Product product = optionalProduct.get();
        if (!product.getAccount().getId().equals(currentUser.getId())) { //요청한 사용자가 상품을 등록한 사용자인지
            return new ResponseEntity<>(getErrorMap(INVALIDUSER), HttpStatus.BAD_REQUEST);
        }
        product.update(productRequestDto);
        ProductResponseDto responseDto = new ProductResponseDto(product);
        ProductResource productResource = new ProductResource(responseDto);
        productResource.add(linkTo(ProductController.class).withRel("query-products"));
        productResource.add(linkTo(ProductController.class).slash(responseDto.getId()).withRel("delete-product"));
        productResource.add(linkTo(ProductController.class).withRel("create-product"));
        return new ResponseEntity<>(productResource, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> deleteProduct(Long productId, Account currentUser) {
        Optional<Product> optionalProduct = findById(productId);
        if (!optionalProduct.isPresent()) {
            return new ResponseEntity<>(getErrorMap(PRODUCTNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        Product product = optionalProduct.get();
        if (!product.getAccount().getId().equals(currentUser.getId())) {
            return new ResponseEntity<>(getErrorMap(INVALIDUSER), HttpStatus.BAD_REQUEST);
        }
        productRepository.delete(product);
        ProductResource productResource = new ProductResource(new ProductResponseDto(product));
        productResource.add(linkTo(ProductController.class).withRel("create-product"));
        productResource.add(linkTo(ProductController.class).withRel("query-products"));
        return new ResponseEntity<>(productResource, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getProductList(Pageable pageable, PagedResourcesAssembler<Product> assembler) {
        Page<Product> all = productRepository.findAll(pageable);
        PagedModel<ProductResource> productResources = assembler.toModel(all, e -> new ProductResource(new ProductResponseDto(e)));

        return ResponseEntity.ok(productResources);
    }

    Optional<Product> findById(Long productId) {
        return productRepository.findById(productId);
    }

    private Map<String, List<String>> getErrorMap(String message) {
        Map<String, List<String>> errors = new HashMap<>();
        errors.put(KEY, Arrays.asList(message));
        return errors;
    }
}
