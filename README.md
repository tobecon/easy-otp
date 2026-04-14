# EasyOTP

一个简单易用的 Android OTP (一次性密码) 认证应用，支持 TOTP 算法和模拟蓝牙键盘输入。

## 目录结构

```
.github/
  └── workflows/
      └── android.yml         # GitHub Actions Android CI
app/
  ├── build.gradle
  ├── proguard-rules.pro
  └── src/main/
      ├── AndroidManifest.xml
      ├── java/com/easyotp/
      └── res/
build.gradle
gradle.properties
gradle/
gradlew
settings.gradle
.gitignore
README.md
```

## 功能特性

- 2FA OTP 认证管理
- TOTP 动态验证码生成
- 本地账户存储与加密数据管理
- 蓝牙键盘模拟自动输入验证码
- GitHub Actions 自动构建 Release APK

## 本地编译

本地编译前请确保已安装 Android SDK，并设置 `ANDROID_HOME` 或在项目根目录中创建 `local.properties`：

```properties
sdk.dir=/path/to/Android/Sdk
```

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## GitHub Actions

仓库已配置 `.github/workflows/android.yml`，支持：

- push 到 `main` 分支
- pull request 到 `main`
- 推送标签 `v*`
- 手动触发 `workflow_dispatch`

构建成功后会上传 `app/build/outputs/apk/release/*.apk` 作为构建产物。

## Release 输出

CI 生成的 APK 可在 GitHub Actions 页面下载；也可直接使用 `./gradlew assembleRelease` 本地生成。
