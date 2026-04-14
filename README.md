# EasyOTP - 简易 OTP 认证应用

一个简单易用的 Android OTP (一次性密码) 认证应用，支持 TOTP 算法，可模拟蓝牙键盘输入验证码。

## 功能特性

1. **2FA 认证管理**
   - 添加、编辑、删除 OTP 账户
   - 支持 TOTP (基于时间的一次性密码)
   - 自动刷新验证码（每秒更新）
   - 数据本地存储，安全可靠

2. **蓝牙键盘模拟**
   - 当手机连接到电脑（通过蓝牙）时
   - 点击验证码可自动输入到电脑
   - 支持剪贴板备份方案

3. **GitHub Actions 自动编译**
   - 推送代码自动构建 APK
   - 支持 Release 版本发布
   - 自动生成并上传 APK 文件

## 项目结构

```
├── app/
│   ├── src/main/
│   │   ├── java/com/easyotp/
│   │   │   ├── MainActivity.java          # 主界面
│   │   │   ├── adapter/
│   │   │   │   └── OTPAdapter.java        # 列表适配器
│   │   │   ├── model/
│   │   │   │   └── OTPAccount.java        # 账户数据模型
│   │   │   └── util/
│   │   │       ├── TOTPGenerator.java     # TOTP 生成器
│   │   │       ├── OTPDataManager.java    # 数据管理
│   │   │       ├── HIDKeyboard.java       # 蓝牙键盘工具
│   │   │       └── BluetoothKeyboardService.java
│   │   ├── res/
│   │   │   ├── layout/                    # 布局文件
│   │   │   ├── values/                    # 资源值
│   │   │   └── drawable/                  # 图标资源
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── .github/workflows/
│   └── android.yml                        # GitHub Actions 配置
├── build.gradle
├── settings.gradle
└── gradlew                                # Gradle 包装器
```

## 使用方法

### 添加账户
1. 点击右下角 "+" 按钮
2. 输入发行商名称（如 Google）
3. 输入账户名（如邮箱）
4. 输入密钥（从服务商获取的 Base32 密钥）
5. 点击"Add"保存

### 使用验证码
- **点击**验证码：如果连接了蓝牙键盘，自动输入；否则复制到剪贴板
- **长按**验证码：显示编辑/删除选项

### 下拉刷新
- 下拉列表可手动刷新所有验证码

## 编译说明

### 本地编译
```bash
./gradlew assembleDebug      # 编译调试版
./gradlew assembleRelease    # 编译发布版
```

### GitHub Actions 自动编译
1. 将代码推送到 GitHub 仓库
2. 自动触发 Build 工作流
3. 在 Actions 页面下载生成的 APK

### 创建 Release 版本
```bash
git tag v1.0.0
git push origin v1.0.0
```
推送标签后会自动创建 Release 并上传 APK。

## 权限说明

- `BLUETOOTH` / `BLUETOOTH_ADMIN`: 检测蓝牙键盘连接
- `BLUETOOTH_CONNECT` / `BLUETOOTH_SCAN`: Android 12+ 蓝牙权限
- `VIBRATE`: 操作反馈

## 注意事项

1. **蓝牙键盘功能**: 需要手机支持 USB Host 模式，并且已配对蓝牙键盘设备
2. **数据安全**: 所有数据存储在本地，不会上传到服务器
3. **备份建议**: 请妥善备份密钥信息，以防数据丢失

## 技术栈

- Android SDK 21+
- Material Design 组件
- TOTP 算法实现
- RecyclerView + SwipeRefreshLayout
- Gson 数据序列化
- GitHub Actions CI/CD

## License

MIT License
