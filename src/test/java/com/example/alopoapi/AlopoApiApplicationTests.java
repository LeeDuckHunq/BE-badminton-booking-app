package com.example.alopoapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.migrations.enabled=false")
class AlopoApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
