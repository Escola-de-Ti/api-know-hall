package br.com.escoladeti.api_know_hall;

import br.com.escoladeti.api_know_hall.service.ImagemService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class ApiKnowHallApplicationTests {

  @MockitoBean
  private ImagemService imagemService;

  @Test
  void contextLoads() {}
}
