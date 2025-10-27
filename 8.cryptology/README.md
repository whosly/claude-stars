# 密码学工程实践

基于 Bouncy Castle 和 Java Security 实现的密码学工程实践项目，包含多种加密算法的实现和应用。

## 项目概述

密码学模块旨在提供一套完整的数据加密解决方案，支持多种数据类型和加密算法，包括：
- 字符型数据加密（AES、SM4）
- 长整型数据加密（FPE）
- 日期型数据加密（FPE）

## 核心技术栈
- **JDK**: 17+
- **加密库**: Bouncy Castle 1.78.1
- **辅助库**: Hutool 5.7.12, Guava 33.3.0-jre
- **构建工具**: Maven 3.6.3 或更高版本
- **测试框架**: JUnit 5.11.2

## 模块结构

### data-type-encryption
数据类型加密模块，提供多种数据类型的加密实现：

#### 字符型加密
- **AES加密**: 支持多种加密模式（CBC、ECB、CTR、OFB、CFB）和填充模式（NoPadding、PKCS7Padding）
- **SM4加密**: 国密SM4算法实现，支持多种加密模式和填充模式

#### 数值型加密
- **长整型FPE加密**: 基于Feistel网络实现的格式保留加密，保持加密前后数据格式一致
- **日期型FPE加密**: 日期格式保留加密，支持自定义日期范围

## 功能特性

### 多种加密算法支持
1. **AES加密**: 支持128位、256位密钥长度，多种加密模式和填充模式
2. **SM4加密**: 国密标准算法，128位密钥长度
3. **FPE加密**: 格式保留加密，保持加密前后数据格式和长度不变

### 加密模式支持
- **CBC模式**: 密码块链接模式，提供良好的安全性
- **ECB模式**: 电子密码本模式，简单但安全性较低
- **CTR模式**: 计数器模式，支持并行处理
- **OFB模式**: 输出反馈模式，将块密码转换为流密码
- **CFB模式**: 密码反馈模式，自同步流密码

### 填充模式支持
- **NoPadding**: 无填充模式，通过智能填充处理确保数据完整性
- **PKCS7Padding**: PKCS#7填充标准
- **PKCS5Padding**: PKCS#5填充标准

### NoPadding模式优化
AES和SM4加密算法在NoPadding模式下通过智能填充处理，确保任意长度数据的安全加密和准确解密。

### 格式保留加密(FPE)
- **长整型FPE**: 支持完整的64位长整型范围加密
- **日期型FPE**: 支持自定义日期范围的格式保留加密
- **可配置性**: 支持自定义密钥、日期范围、加密轮数等参数

## 使用说明

### 构建项目
```bash
# 进入密码学模块目录
cd 8.cryptology

# 构建所有子模块
mvn clean install

# 构建特定子模块
mvn install -pl data-type-encryption
```

### AES加密示例
```java
// 创建AES加密实例
AESCrypto aesCrypto = new AESCrypto();

// 加密数据
byte[] encrypted = aesCrypto.encrypt(
    "Hello World", 
    "0123456701234567".getBytes(),  // 128位密钥
    BitLenMode._128,                // 密钥长度模式
    EncMode.CBC,                    // 加密模式
    PaddingMode.PKCS7Padding,       // 填充模式
    Optional.empty()                // IV向量（CBC模式需要）
);

// 解密数据
byte[] decrypted = aesCrypto.decrypt(
    encrypted,
    "0123456701234567".getBytes(),
    BitLenMode._128,
    EncMode.CBC,
    PaddingMode.PKCS7Padding,
    Optional.empty()
);
```

### SM4加密示例
```java
// 创建SM4加密实例
SM4Crypto sm4Crypto = new SM4Crypto();

// 加密数据
byte[] encrypted = sm4Crypto.encrypt(
    "Hello World",
    "0123456701234567".getBytes(),  // 128位密钥
    EncMode.CBC,                    // 加密模式
    PaddingMode.PKCS7Padding,       // 填充模式
    Optional.empty()                // IV向量
);

// 解密数据
byte[] decrypted = sm4Crypto.decrypt(
    encrypted,
    "0123456701234567".getBytes(),
    EncMode.CBC,
    PaddingMode.PKCS7Padding,
    Optional.empty()
);
```

### 长整型FPE加密示例
```java
// 创建长整型FPE加密实例
LongFPECrypto longFPECrypto = new LongFPECrypto();

// 加密长整型数据
Long original = 123456789L;
Long encrypted = longFPECrypto.encrypt(original);

// 解密长整型数据
Long decrypted = longFPECrypto.decrypt(encrypted);

// 验证结果
assert original.equals(decrypted);
```

### 日期型FPE加密示例
```java
// 配置FPE参数
FPEConfig config = new FPEConfig.Builder()
    .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==")  // 16字节密钥的Base64
    .dateRange(LocalDate.of(2000, 1, 1), LocalDate.of(2030, 12, 31))
    .rounds(10)
    .build();

// 创建日期FPE实例
ILocalDateFPE dateFPE = config.createFPE();

// 加密日期
LocalDate original = LocalDate.of(2023, 6, 15);
LocalDate encrypted = dateFPE.encrypt(original);

// 解密日期
LocalDate decrypted = dateFPE.decrypt(encrypted);

// 验证结果
assert original.equals(decrypted);
```

## 测试

模块包含完整的单元测试，覆盖各种加密算法和模式：

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=AESCryptoTest
```

## 相关资源

- [Bouncy Castle 官方文档](https://www.bouncycastle.org/documentation.html)
- [Java Cryptography Architecture (JCA) Reference Guide](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html)
- [SM4 国密算法标准](http://www.gb688.cn/bzgk/gb/newGbInfo?hcno=7803DE42D3BC5E80B81442210AA04495)
- [NIST FPE 标准](https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-38G.pdf)