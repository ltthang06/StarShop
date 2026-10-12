package vn.iotstar.starshop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerReviewCard {

    private String author;
    private int rating;
    private String content;
    private String imageUrl;
    private String videoUrl;
    private String createdAt;
}
