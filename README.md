# Environment Configuration

## Aliyun OSS Setup
This project uses Aliyun OSS for file storage. To run the application, you need to configure the following environment variables:

```
ALIYUN_ACCESS_KEY_ID=your_access_key_id
ALIYUN_ACCESS_KEY_SECRET=your_access_key_secret
ALIYUN_OSS_BUCKET=your_bucket_name
ALIYUN_OSS_ENDPOINT=your_endpoint
```

### Setting up environment variables:

1. For Windows PowerShell (temporary):
```powershell
$env:ALIYUN_ACCESS_KEY_ID="your_access_key_id"
$env:ALIYUN_ACCESS_KEY_SECRET="your_access_key_secret"
$env:ALIYUN_OSS_BUCKET="your_bucket_name"
$env:ALIYUN_OSS_ENDPOINT="your_endpoint"
```

2. For permanent configuration:
- Open Windows Settings
- Go to System > About > Advanced system settings
- Click on Environment Variables
- Add the variables under your user variables

Note: Never commit actual credentials to version control. Keep your access keys secure. 