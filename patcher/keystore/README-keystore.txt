Keystore file: patcher/keystore/debug.keystore
storePassword=androiddebug
keyAlias=androiddebugkey
keyPassword=androiddebug

Generate with:
keytool -genkeypair -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -keystore debug.keystore -storepass androiddebug -keypass androiddebug -dname 'CN=Android Debug,O=Android,C=US'
