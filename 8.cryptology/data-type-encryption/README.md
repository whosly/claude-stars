# 数据类型值加密的方法

数据类型加密模块提供了针对不同数据类型的加密实现，包括字符型、长整型和日期型数据的加密解决方案。

## 项目概述

该模块实现了多种数据类型的加密算法，重点解决以下问题：
1. 不同数据类型的加密需求
2. 格式保留加密(FPE)的实现
3. 国密算法的支持
4. 多种加密模式和填充模式的支持

## 核心组件

### 字符型加密 (chars包)
提供AES和SM4两种主流加密算法的实现：

#### AES加密 (aes包)
- 支持128位、256位密钥长度
- 支持CBC、ECB、CTR、OFB、CFB五种加密模式
- 支持NoPadding、PKCS7Padding、PKCS5Padding三种填充模式
- 使用Bouncy Castle提供者实现
- NoPadding模式下通过智能填充处理确保数据完整性

#### SM4加密 (sm4包)
- 国密标准算法实现
- 支持128位密钥长度
- 支持CBC、ECB、CTR、OFB、CFB五种加密模式
- 支持NoPadding、PKCS7Padding、PKCS5Padding三种填充模式
- NoPadding模式下通过智能填充处理确保数据完整性

### 数值型加密 (longx包)
提供长整型格式保留加密实现：

#### LongFPECrypto
- 基于Feistel网络的格式保留加密
- 支持有符号64位长整型的完整范围加密
- 使用AES作为伪随机函数
- 保持加密前后数据格式和长度不变

### 日期型加密 (datex包)
提供日期类型格式保留加密实现：

#### LocalDateFPEImpl
- 基于Feistel网络的日期格式保留加密
- 支持自定义日期范围
- 可配置加密轮数
- 支持批量加密解密操作
- 使用AES作为伪随机函数

## NoPadding模式支持

AES和SM4加密算法在NoPadding模式下进行了特殊处理，通过[NoPaddingSmartFixed](./src/main/java/com/whosly/stars/cryptology/data/chars/NoPaddingSmartFixed.java)类实现智能填充和移除：

1. **智能填充**: 自动计算需要填充的长度，确保总长度是块大小的倍数
2. **填充标记**: 使用特殊标记和长度信息来标识填充边界
3. **准确移除**: 通过读取长度信息来确定原始数据的边界，准确移除填充数据
4. **数据完整性**: 通过长度信息验证确保数据完整性

这种实现方式使得NoPadding模式可以安全地处理任意长度的数据，而不会出现数据截断或填充错误的问题。

## 枚举类型

### BitLenMode
定义密钥长度模式：
- _56: 56位密钥
- _128: 128位密钥
- _256: 256位密钥
- _512: 512位密钥
- _1024: 1024位密钥
- _2048: 2048位密钥
- _5120: 5120位密钥

### EncMode
定义加密模式：
- CBC: 密码块链接模式
- ECB: 电子密码本模式
- CTR: 计数器模式
- OFB: 输出反馈模式
- CFB: 密码反馈模式

### PaddingMode
定义填充模式：
- NoPadding: 无填充
- PKCS7Padding: PKCS#7填充
- PKCS5Padding: PKCS#5填充

## 工具类

### NoPaddingSmartFixed
NoPadding模式下的智能填充处理：
- 自动计算填充长度
- 通过特殊标记和长度信息标识填充边界
- 准确移除填充数据

### Keys
密钥生成工具类：
- 生成AES密钥
- 支持128位密钥长度

## 使用示例

### AES加密使用示例
```java
AESCrypto aesCrypto = new AESCrypto();
byte[] encrypted = aesCrypto.encrypt(
    "Hello World",
    "0123456701234567".getBytes(),
    BitLenMode._128,
    EncMode.CBC,
    PaddingMode.PKCS7Padding,
    Optional.empty()
);
```

### SM4加密使用示例
```java
SM4Crypto sm4Crypto = new SM4Crypto();
byte[] encrypted = sm4Crypto.encrypt(
    "Hello World",
    "0123456701234567".getBytes(),
    EncMode.CBC,
    PaddingMode.PKCS7Padding,
    Optional.empty()
);
```

### 长整型FPE加密使用示例
```java
LongFPECrypto longFPECrypto = new LongFPECrypto();
Long encrypted = longFPECrypto.encrypt(123456789L);
Long decrypted = longFPECrypto.decrypt(encrypted);
```

### 日期型FPE加密使用示例
```java
FPEConfig config = new FPEConfig.Builder()
    .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==")
    .dateRange(LocalDate.of(2000, 1, 1), LocalDate.of(2030, 12, 31))
    .rounds(10)
    .build();

ILocalDateFPE dateFPE = config.createFPE();
LocalDate encrypted = dateFPE.encrypt(LocalDate.of(2023, 6, 15));
LocalDate decrypted = dateFPE.decrypt(encrypted);
```

## 测试

模块包含完整的单元测试，确保各种加密算法和模式的正确性：

- AES加密解密测试
- SM4加密解密测试
- 长整型FPE加密解密测试
- 日期型FPE加密解密测试

```
=== AES加密 测试结果 ===
BitLenMode      EncMode    PaddingMode     Result    
--------------------------------------------------------
_128            CBC        NoPadding       PASS      
_128            CBC        NoPadding       PASS      
_128            CBC        PKCS5Padding    PASS      
_128            CBC        PKCS5Padding    PASS      
_128            CBC        PKCS7Padding    PASS      
_128            CBC        PKCS7Padding    PASS      
_128            CFB        NoPadding       PASS      
_128            CFB        NoPadding       PASS      
_128            CFB        PKCS5Padding    PASS      
_128            CFB        PKCS5Padding    PASS      
_128            CFB        PKCS7Padding    PASS      
_128            CFB        PKCS7Padding    PASS      
_128            CTR        NoPadding       PASS      
_128            CTR        NoPadding       PASS      
_128            CTR        PKCS5Padding    PASS      
_128            CTR        PKCS5Padding    PASS      
_128            CTR        PKCS7Padding    PASS      
_128            CTR        PKCS7Padding    PASS      
_128            ECB        NoPadding       PASS      
_128            ECB        NoPadding       PASS      
_128            ECB        PKCS5Padding    PASS      
_128            ECB        PKCS5Padding    PASS      
_128            ECB        PKCS7Padding    PASS      
_128            ECB        PKCS7Padding    PASS      
_128            OFB        NoPadding       PASS      
_128            OFB        NoPadding       PASS      
_128            OFB        PKCS5Padding    PASS      
_128            OFB        PKCS5Padding    PASS      
_128            OFB        PKCS7Padding    PASS      
_128            OFB        PKCS7Padding    PASS      
_256            CBC        NoPadding       PASS      
_256            CBC        NoPadding       PASS      
_256            CBC        PKCS5Padding    PASS      
_256            CBC        PKCS5Padding    PASS      
_256            CBC        PKCS7Padding    PASS      
_256            CBC        PKCS7Padding    PASS      
_256            CFB        NoPadding       PASS      
_256            CFB        NoPadding       PASS      
_256            CFB        PKCS5Padding    PASS      
_256            CFB        PKCS5Padding    PASS      
_256            CFB        PKCS7Padding    PASS      
_256            CFB        PKCS7Padding    PASS      
_256            CTR        NoPadding       PASS      
_256            CTR        NoPadding       PASS      
_256            CTR        PKCS5Padding    PASS      
_256            CTR        PKCS5Padding    PASS      
_256            CTR        PKCS7Padding    PASS      
_256            CTR        PKCS7Padding    PASS      
_256            ECB        NoPadding       PASS      
_256            ECB        NoPadding       PASS      
_256            ECB        PKCS5Padding    PASS      
_256            ECB        PKCS5Padding    PASS      
_256            ECB        PKCS7Padding    PASS      
_256            ECB        PKCS7Padding    PASS      
_256            OFB        NoPadding       PASS      
_256            OFB        NoPadding       PASS      
_256            OFB        PKCS5Padding    PASS      
_256            OFB        PKCS5Padding    PASS      
_256            OFB        PKCS7Padding    PASS      
_256            OFB        PKCS7Padding    PASS      
========================================================
```

