package org.example.edusoft.common.storage;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.example.edusoft.common.domain.FileBo;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SimpleFileStorageProvider implements IFileStorageProvider {
    @Override 
    public IFileStorage getStorage() {
        // 返回一个简单的临时实现
        return new IFileStorage() {
            @Override
            public FileBo upload(MultipartFile file) {
                // 临时实现：返回一个包含基本信息的 FileBo
                FileBo bo = new FileBo();
                bo.setUrl("/temp/" + file.getOriginalFilename());
                bo.setSize(file.getSize());
                return bo;
            }

            @Override
            public void download(String url, HttpServletResponse response) {
                // 临时实现：不做任何事
            }

            @Override
            public String getUrl(String filePath) {
                // 临时实现：直接返回文件路径
                return filePath;
            }

            @Override
            public void delete(String filePath) {
                // 临时实现：不做任何事
            }

            @Override
            public void download(String url, java.io.OutputStream outputStream) {
                // 临时实现：不做任何事
            }

            @Override
            public boolean bucketExists(String bucketName) {
                // 临时实现：始终返回 true
                return true;
            }

            @Override
            public void makeBucket(String bucketName) {
                // 临时实现：不做任何事
            }
        };
    }
}