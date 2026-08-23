package FYP.project_backend.language;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/languages")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class LanguageController {

    private final ObjectMapper objectMapper;

    // =====================================================
    // GET TRANSLATIONS
    // =====================================================

    @GetMapping("/{language}")
    public Map<String, String> getTranslations(
            @PathVariable String language
    ) {

        String normalizedLanguage =
                language.toLowerCase().trim();

        // =================================================
        // ONLY ENGLISH AND SWAHILI
        // =================================================

        if (!normalizedLanguage.equals("en")
                && !normalizedLanguage.equals("sw")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported language"
            );
        }

        // =================================================
        // LANGUAGE FILE
        // =================================================

        String fileName =
                "i18n/" + normalizedLanguage + ".json";

        try {

            ClassPathResource resource =
                    new ClassPathResource(fileName);

            if (!resource.exists()) {

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Language file not found"
                );
            }

            try (InputStream inputStream =
                         resource.getInputStream()) {

                return objectMapper.readValue(
                        inputStream,
                        new TypeReference<Map<String, String>>() {}
                );
            }

        } catch (IOException e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to load language translations",
                    e
            );
        }
    }
}