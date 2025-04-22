package at.fhburgenland.bookmanager.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BookUpdateRequest {
    @Size(max = 255)
    private String title;

    private List<String> authors;

    @Size(max = 5000)
    private String description;

    @Size(max = 2048)
    private String coverUrl;
}
