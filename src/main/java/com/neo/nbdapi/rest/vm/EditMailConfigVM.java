package com.neo.nbdapi.rest.vm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditMailConfigVM {

    @NotEmpty(message = "id không được trống")
    private String id;

    /**
     * ip cua mail
     */
    @NotEmpty(message = "Địa chỉ ip không được trống")
    @Size(max = 100, message = "Ip dài tối đa 100 ký tự")
    private String ip;

    /**
     * port mail
     */
    @NotEmpty(message = "Port không được trống")
    @Size(max = 50, message = "Port dài tối đa 50 ký tự")
    @Pattern(regexp = "^\\d+$", message = "Port phải là số")
    private String port;

    /**
     * ten dang nhap cua mail
     */
    @NotEmpty(message = "Username không được trống")
    @Size(max = 30, message = "Username dài tối đa 30 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_*&@#!%$]*$", message = "Tên đăng nhập chỉ gồm A-Z 0-9 và ký tự đặc biệt")
    private String username;

    /**
     * mat khau cua mail
     */
    @NotEmpty(message = "password không được trống")
    @Size(max = 100, message = "password dài tối đa 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_*&@#!%$]*$", message = "Mật khẩu chỉ gồm A-Z 0-9 và ký tự đặc biệt")
    private String password;

    /**
     * domain cua mail
     */
    @NotEmpty(message = "domain không được trống")
    @Size(max = 100, message = "domain dài tối đa 100 ký tự")
    private String domain;

    /**
     * nguoi gui
     */
    @NotEmpty(message = "senderName không được trống")
    @Size(max = 100, message = "senderName dài tối đa 100 ký tự")
    private String senderName;

    /**
     * dia chi email
     */
    @NotEmpty(message = "email không được trống")
    @Size(max = 100, message = "email dài tối đa 100 ký tự")
    @Email(message = "Email không đúng định dạng")
    private String email;

    /**
     * giao thuc
     */
    @NotEmpty(message = "protocol không được trống")
    @Size(max = 100, message = "protocol dài tối đa 100 ký tự")
    private String protocol;
    
}
