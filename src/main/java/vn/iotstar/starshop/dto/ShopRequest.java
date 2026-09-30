package vn.iotstar.starshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShopRequest {

    @NotBlank(message = "Tên shop không được để trống")
    @Size(max = 150, message = "Tên shop tối đa 150 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    private String email;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    private String logo;

    private String banner;
}