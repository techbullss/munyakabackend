package com.example.munyaka.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ReturnRequest {
    private List<ReturnItemDTO> items;
}

