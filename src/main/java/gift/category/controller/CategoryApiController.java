package gift.category.controller;

import gift.category.dto.CategoryIdDto;
import gift.category.dto.CategoryRequestDto;
import gift.category.dto.CategoryResponseDto;
import gift.category.service.CategoryService;
import gift.global.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CategoryApiController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryApiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 카테고리를 삽입하는 핸들러
    @PostMapping
    public ApiResponseDto createCategory(@RequestBody CategoryRequestDto categoryRequestDto) {
        categoryService.insertCategory(categoryRequestDto);

        return ApiResponseDto.of();
    }

    // 카테고리를 조회하는 핸들러. 프런트에서 제품 추가 명령 중, 카테고리를 제시해 주기 위해서 만들었습니다.
    @GetMapping
    public ApiResponseDto readCategory() {
        List<CategoryResponseDto> categories = categoryService.selectCategories();

        return ApiResponseDto.of(categories);
    }

    // 카테고리를 수정하는 핸들러
    @PutMapping("/{category-id}")
    public ApiResponseDto updateCategory(@PathVariable(name = "category-id") Long categoryId,
        @RequestBody CategoryRequestDto categoryRequestDto) {
        categoryService.updateCategory(new CategoryIdDto(categoryId), categoryRequestDto);

        return ApiResponseDto.of();
    }

    @DeleteMapping("/{category-id}")
    public ApiResponseDto deleteCategory(@PathVariable(name = "category-id") Long categoryId) {
        categoryService.deleteCategory(new CategoryIdDto(categoryId));

        return ApiResponseDto.of();
    }
}
