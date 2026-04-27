package com.bookvault.library.controller;

import com.bookvault.library.model.Address;
import com.bookvault.library.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepository;

    @GetMapping
    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }
}