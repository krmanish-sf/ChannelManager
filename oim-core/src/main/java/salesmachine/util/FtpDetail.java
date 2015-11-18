package salesmachine.util;

import salesmachine.oim.api.OimConstants;

public class FtpDetail {
  String accountNumber;
  String url;
  String userName;
  String password;
  String ftpType;
  int supplierId;


  public WareHouseType wareHouseType;

  public static enum WareHouseType {
    PHI(OimConstants.SUPPLIER_METHOD_TYPE_HG_PHI), HVA(OimConstants.SUPPLIER_METHOD_TYPE_HG_HVA);
    private final int warehouseType;

    WareHouseType(int warehouseType) {
      this.warehouseType = warehouseType;
    }

    public int getWharehouseType() {
      return warehouseType;
    }
  }

  public FtpDetail() {
    super();
  }

  public FtpDetail(String accountNumber, String url, String userName, String password) {
    this.accountNumber = accountNumber;
    this.url = url;
    this.userName = userName;
    this.password = password;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public String getFtpType() {
    return ftpType;
  }

  public void setFtpType(String ftpType) {
    this.ftpType = ftpType;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public WareHouseType getWhareHouseType() {
    return wareHouseType;
  }

  public void setWhareHouseType(WareHouseType whareHouseType) {
    this.wareHouseType = whareHouseType;
  }



  @Override
  public String toString() {
    return "FtpDetail [accountNumber=" + accountNumber + ", url=" + url + ", userName=" + userName
        + ", password=" + password + ", wareHouseType=" + wareHouseType + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((accountNumber == null) ? 0 : accountNumber.hashCode());
    result = prime * result + ((password == null) ? 0 : password.hashCode());
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    result = prime * result + ((userName == null) ? 0 : userName.hashCode());
    result = prime * result + ((wareHouseType == null) ? 0 : wareHouseType.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FtpDetail other = (FtpDetail) obj;
    if (accountNumber == null) {
      if (other.accountNumber != null)
        return false;
    } else if (!accountNumber.equals(other.accountNumber))
      return false;
    if (password == null) {
      if (other.password != null)
        return false;
    } else if (!password.equals(other.password))
      return false;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    if (userName == null) {
      if (other.userName != null)
        return false;
    } else if (!userName.equals(other.userName))
      return false;
    if (wareHouseType != other.wareHouseType)
      return false;
    return true;
  }

  public int getSupplierId() {
    return supplierId;
  }

  public void setSupplierId(int supplierId) {
    this.supplierId = supplierId;
  }
}

