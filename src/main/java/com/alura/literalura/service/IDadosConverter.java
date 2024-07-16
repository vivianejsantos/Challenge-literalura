package com.alura.literalura.service;

public interface IDadosConverter {
    <T> T getData(String json, Class<T> classe);
}
