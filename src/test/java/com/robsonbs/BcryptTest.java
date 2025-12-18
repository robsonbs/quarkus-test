package com.robsonbs;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class BcryptTest {
    @Test
    void testBcryptHashMatchesDemoPassword() {
        // Hash usado na migração V7 para os usuários demo
        String demoHash = "$2a$10$hibzY57sqKAYX6Qqo8SPXetkj/.c.eyRUw99WwW8zGZOYQAIOxbB6";
        
        assertTrue(BcryptUtil.matches("123", demoHash), 
            "Senha '123' deve corresponder ao hash dos usuários demo");
    }
}
