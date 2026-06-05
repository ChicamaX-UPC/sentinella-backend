package com.chicamax.sentinella.alerts.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** @deprecated Use {@code sentinella-monitoring-service} /v1/alert-rules */
@Deprecated
@RestController
@RequestMapping("/v1/alert-rules")
public class AlertRulesController {

    private static void gone() {
        throw new ResponseStatusException(
                HttpStatus.GONE,
                "Reglas de umbral migradas a Monitoring (/api/v1/alert-rules)."
        );
    }

    @GetMapping
    public void list() {
        gone();
    }

    @GetMapping("/{ruleId}")
    public void get(@PathVariable String ruleId) {
        gone();
    }

    @PostMapping
    public void create() {
        gone();
    }

    @PutMapping("/{ruleId}")
    public void update(@PathVariable String ruleId) {
        gone();
    }

    @DeleteMapping("/{ruleId}")
    public void delete(@PathVariable String ruleId) {
        gone();
    }
}
