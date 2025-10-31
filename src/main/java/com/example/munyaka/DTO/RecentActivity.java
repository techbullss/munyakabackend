package com.example.munyaka.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivity {
    private String type;         // e.g. "sale", "payment", etc.
    private String title;        // e.g. "New sale completed"
    private String description;  // e.g. "Sale #45"
    private String amount;       // e.g. "KSh 5000"
    private String timeAgo;      // e.g. "2 hours ago"
}
