package com.example.demo.messager.classes;

public record Message (
    int id,
    String sender,
    String text,
    String time
) { }
