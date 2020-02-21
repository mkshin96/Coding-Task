package me.mugon.lendit.web;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.api.ProductService;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.CurrentUser;
import me.mugon.lendit.domain.product.Product;
import me.mugon.lendit.web.dto.product.ProductRequestDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static me.mugon.lendit.api.error.ErrorMessageConstant.KEY;

@RequiredArgsConstructor
@RequestMapping(value = "/api/products", produces = MediaTypes.HAL_JSON_VALUE)
@RestController
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<?> getProductList(Pageable pageable, PagedResourcesAssembler<Product> assembler) {
        return productService.getProductList(pageable, assembler);
    }

    @PostMapping
    public ResponseEntity<?> registrationProduct(@Valid @RequestBody ProductRequestDto productRequestDto, Errors errors, @CurrentUser Account currentUser) {
        if (errors.hasErrors()) {
            return returnError(errors);
        }
        return productService.registrationProduct(productRequestDto, currentUser);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId, @Valid @RequestBody ProductRequestDto productRequestDto, Errors errors, @CurrentUser Account currentUser) {
        if (errors.hasErrors()) {
            return returnError(errors);
        }
        return productService.updateProduct(productId, productRequestDto, currentUser);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId, @CurrentUser Account currentUser) {
        return productService.deleteProduct(productId, currentUser);
    }

    private ResponseEntity<?> returnError(Errors errors) {
        Map<String, List<String>> errorMap = new HashMap<>();
        errors.getFieldErrors().forEach(e -> {
            if (errorMap.get(KEY) != null) {
                errorMap.get(KEY).add(e.getField() + " 가 유효하지 않습니다.");
            } else {
                List<String> list = new LinkedList<>();
                list.add(e.getField() + " 가 유효하지 않습니다.");
                errorMap.put(KEY, list);
            }
        });

        errors.getGlobalErrors().forEach(e -> {
            if (errorMap.get(KEY) != null) {
                errorMap.get(KEY).add(e.getObjectName() + " 가 유효하지 않습니다.");
            } else {
                List<String> list = new LinkedList<>();
                list.add(e.getObjectName() + " 가 유효하지 않습니다.");
                errorMap.put(KEY, list);
            }
        });
        return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
    }
}
