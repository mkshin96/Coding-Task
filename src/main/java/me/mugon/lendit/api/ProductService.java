package me.mugon.lendit.api;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.api.error.ErrorMessage;
import me.mugon.lendit.domain.product.Product;
import me.mugon.lendit.domain.product.ProductRepository;
import me.mugon.lendit.web.dto.ProductRequestDto;
import me.mugon.lendit.web.dto.ProductResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static me.mugon.lendit.api.error.ErrorMessageConstant.KEY;
import static me.mugon.lendit.api.error.ErrorMessageConstant.PRODUCTNOTFOUND;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ResponseEntity<?> registrationProduct(ProductRequestDto productRequestDto) {
        Product savedProduct = productRepository.save(productRequestDto.toEntity());
        return new ResponseEntity<>(new ProductResponseDto(savedProduct), HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<?> updateProduct(Long productId, ProductRequestDto productRequestDto) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (!optionalProduct.isPresent()) {
            return new ResponseEntity<>(getErrorMap(PRODUCTNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        Product product = optionalProduct.get();
        product.update(productRequestDto);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> deleteProduct(Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (!optionalProduct.isPresent()) {
            return new ResponseEntity<>(getErrorMap(PRODUCTNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        productRepository.delete(optionalProduct.get());
        return ResponseEntity.ok().build();
    }

    private Map<String, List<String>> getErrorMap(String message) {
        Map<String, List<String>> errors = new HashMap<>();
        errors.put(KEY, Arrays.asList(message));
        return errors;
    }

    public ResponseEntity<?> getProductList() {
        List<Product> allProduct = productRepository.findAll();
        List<ProductResponseDto> collect = allProduct.stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(collect, HttpStatus.OK);
    }
}
