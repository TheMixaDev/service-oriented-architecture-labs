package com.aeeph.routeservice.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.math.BigDecimal;
import javax.persistence.Embeddable;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Embeddable
@JacksonXmlRootElement(localName = "Coordinates")
public class Coordinates {

  @DecimalMax(value = "716", inclusive = true, message = "Максимальное значение для поля x - 716")
  private BigDecimal x; // Максимальное значение поля: 716

  @NotNull(message = "Не заполнено поле y")
  private Integer y; // Поле не может быть null

  public BigDecimal getX() {
    return x;
  }

  public void setX(BigDecimal x) {
    this.x = x;
  }

  public Integer getY() {
    return y;
  }

  public void setY(Integer y) {
    this.y = y;
  }
}
