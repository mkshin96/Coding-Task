package me.mugon.lendit.api;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.product.Product;
import me.mugon.lendit.domain.product.ProductRepository;
import me.mugon.lendit.web.dto.product.ProductRequestDto;
import me.mugon.lendit.web.dto.product.ProductResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static me.mugon.lendit.api.error.ErrorMessageConstant.*;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ResponseEntity<?> registrationProduct(ProductRequestDto productRequestDto, Account currentUser) {
        Product product = productRequestDto.toEntity(currentUser);
        product.mapUser(currentUser);
        Product savedProduct = productRepository.save(product);
        return new ResponseEntity<>(new ProductResponseDto(savedProduct), HttpStatus.CREATED);
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
        return new ResponseEntity<>(product, HttpStatus.OK);
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
        return ResponseEntity.ok().build();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getProductList() {
        List<Product> allProduct = productRepository.findAll();
        List<ProductResponseDto> collect = allProduct.stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(collect, HttpStatus.OK);
    }

    public Optional<Product> findById(Long productId) {
        return productRepository.findById(productId);
    }

    private Map<String, List<String>> getErrorMap(String message) {
        Map<String, List<String>> errors = new HashMap<>();
        errors.put(KEY, Arrays.asList(message));
        return errors;
    }
}
