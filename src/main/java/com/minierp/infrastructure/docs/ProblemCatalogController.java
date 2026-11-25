package com.minierp.infrastructure.docs;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minierp.domain.model.shared.NotFoundException;

/**
 * Public documentation of the error contract: every problem type a client can
 * encounter. The RFC 9457 {@code type} URI of error responses points here.
 */
@RestController
@RequestMapping("/api/problems")
public class ProblemCatalogController {

    @GetMapping
    public List<ProblemDescriptor> list() {
        return ProblemCatalog.all();
    }

    @GetMapping("/{slug}")
    public ProblemDescriptor get(@PathVariable String slug) {
        return ProblemCatalog.find(slug)
                .orElseThrow(() -> new NotFoundException("Tipe error '%s' tidak ditemukan".formatted(slug)));
    }
}
