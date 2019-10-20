package com.sixsprints.auth.domain.mock;

import java.util.Calendar;
import java.util.Date;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.sixsprints.core.domain.AbstractMongoEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Document
public class PasswordResetOtp extends AbstractMongoEntity {

  private static final long serialVersionUID = 3907769974134268344L;

  @Indexed(unique = true)
  private String otp;

  private Date expiryDate;

  private User user;

  public void calculateExpiryDate() {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(new Date().getTime());
    cal.add(Calendar.MINUTE, 10);
    this.expiryDate = new Date(cal.getTime().getTime());
  }

}
