package com.example.hellobackend.service;

import com.example.hellobackend.model.Category;
import com.example.hellobackend.model.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptService {

    private static final List<Prompt> MOCK_PROMPTS = List.of(
        new Prompt("1", "Summarize Text",      "Condense any passage into key points",         Category.PRODUCTIVITY),
        new Prompt("2", "Write Unit Tests",     "Generate JUnit tests for a given Java method", Category.DEVELOPMENT),
        new Prompt("3", "Explain Code",         "Break down a code snippet in plain English",   Category.DEVELOPMENT),
        new Prompt("4", "Draft Email",          "Compose a professional email from bullet points", Category.COMMUNICATION),
        new Prompt("5", "Translate to Spanish", "Translate English text into Spanish",          Category.LANGUAGE),
        new Prompt("6", "Generate SQL Query",   "Build a SQL SELECT from a natural-language ask", Category.DATA)
    );

    public List<Prompt> findAll() {
        return MOCK_PROMPTS;
    }
}
