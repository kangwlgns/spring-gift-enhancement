package gift.wishlist.service;

import gift.global.dto.PageInfoDto;
import gift.global.dto.ProductInfoDto;
import gift.global.dto.UserInfoDto;
import gift.product.entity.Product;
import gift.wishlist.dto.WishListIdDto;
import gift.wishlist.dto.WishListResponseDto;
import gift.wishlist.entity.UserProduct;
import gift.wishlist.entity.WishList;
import gift.wishlist.repository.WishListRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishListService {

    private final WishListRepository wishListRepository;

    @Autowired
    public WishListService(WishListRepository wishListRepository) {
        this.wishListRepository = wishListRepository;
    }

    // 위시리스트 추가하는 핸들러
    @Transactional
    public void insertWishProduct(ProductInfoDto productInfoDto, UserInfoDto userInfoDto) {
        UserProduct userProduct = new UserProduct(userInfoDto.userId(), new Product(
            productInfoDto.productId(), productInfoDto.name(), productInfoDto.price(),
            productInfoDto.imageUrl()));

        // 검증하기 (내부 id로 검증하면 쓸 데 없는 조인이 일어나서 객체로 확인해야 함)
        verifyWishProductAlreadyExistence(userProduct);

        WishList wishList = new WishList(userProduct, 1);

        // 이미 위시리스트에 존재하는 제품을 또 추가하는 경우, 이미 담겼다고 경고하기
        wishListRepository.save(wishList);
    }

    // 위시리스트를 읽어오는 핸들러
    @Transactional(readOnly = true)
    public List<WishListResponseDto> readWishProducts(UserInfoDto userInfoDto,
        PageInfoDto pageInfoDto) {
        Long userId = userInfoDto.userId();
        Pageable pageable = pageInfoDto.pageRequest();

        // 특정 userId를 갖는 위시리스트 불러오기
        Page<WishList> wishList = wishListRepository.findByUserProductUserId(userId, pageable);

        return wishList.stream().map(wishProduct -> {
            long wishListId = wishProduct.getWishListId();
            long wishUserId = wishProduct.getUserProduct().getUserId();
            long wishProductId = wishProduct.getUserProduct().getProduct().getProductId();
            String name = wishProduct.getUserProduct().getProduct().getName();
            int price = wishProduct.getUserProduct().getProduct().getPrice();
            String imageUrl = wishProduct.getUserProduct().getProduct().getImage();
            int quantity = wishProduct.getQuantity();

            return new WishListResponseDto(wishListId, wishUserId, wishProductId, name, price,
                imageUrl,
                quantity);
        }).collect(Collectors.toList());
    }

    // 개수 증가하는 핸들러
    @Transactional
    public void increaseWishProduct(WishListIdDto wishListIdDto) {
        Optional<WishList> optionalWishList = wishListRepository.findById(wishListIdDto.wishListId());
        verifyWishProductExistence(optionalWishList);

        WishList actualWishList = optionalWishList.get();
        int afterQuantity = actualWishList.getQuantity() + 1;

        actualWishList.updateQuantity(afterQuantity);
    }

    // 개수 감소하는 핸들러
    @Transactional
    public void decreaseWishProduct(WishListIdDto wishListIdDto) {
        Optional<WishList> optionalWishList = wishListRepository.findById(wishListIdDto.wishListId());
        verifyWishProductExistence(optionalWishList);

        WishList actualWishList = optionalWishList.get();
        int afterQuantity = actualWishList.getQuantity() - 1;

        // 1을 뺀 경우가 0 이하가 되면 제거.
        decreaseOrDelete(actualWishList, afterQuantity);
    }

    // 위시리스트에서 제품을 삭제하는 핸들러
    @Transactional
    public void deleteWishProduct(WishListIdDto wishListIdDto) {
        Long wishListId = wishListIdDto.wishListId();
        verifyWishProductExistence(wishListId);

        wishListRepository.deleteById(wishListId);
    }

    private void decreaseOrDelete(WishList actualWishList, int quantity) {
        if (quantity <= 0) {
            wishListRepository.delete(actualWishList);
            return;
        }

        actualWishList.updateQuantity(quantity);
    }

    // 위시리스트에 제품이 존재하는지 검증
    private void verifyWishProductAlreadyExistence(UserProduct userProduct) {
        if (wishListRepository.existsByUserProduct(userProduct)) {
            throw new IllegalArgumentException("이미 장바구니에 담은 제품입니다.");
        }
    }

    // 불러온 제품이 존재하는지 확인
    private void verifyWishProductExistence(Optional<WishList> optionalWishProduct) {
        if (optionalWishProduct.isEmpty()) {
            throw new IllegalArgumentException("장바구니에 존재하지 않는 제품입니다.");
        }
    }

    // Id를 통해 제품이 존재하는지 확인
    private void verifyWishProductExistence(Long wishListId) {
        if (!wishListRepository.existsById(wishListId)) {
            throw new IllegalArgumentException("이미 삭제된 제품입니다.");
        }
    }
}