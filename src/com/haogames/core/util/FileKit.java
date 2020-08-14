package com.haogames.core.util;

import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import org.redkale.convert.json.JsonConvert;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Arrays.asList;

/**
 * Created by liangxianyou at 2018/5/31 10:23.
 */
public final class FileKit {

    private FileKit() {
    }

    public static void strToFile(String entityBody, File file) {
        strToFile(entityBody, file, true);
    }

    public static void strToFile(String entityBody, File file, boolean existDel) {
        if (file.exists()) {
            if (existDel) {
                file.delete();
            } else {
                throw new RuntimeException(file.getPath() + "已经存在");
            }
        }

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (
                FileOutputStream out = new FileOutputStream(file);
        ) {
            out.write(entityBody.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void append(String str, File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (
                FileOutputStream out = new FileOutputStream(file, true);
        ) {
            out.write(str.getBytes("UTF-8"));
            if (!str.endsWith("\n")) {
                out.write("\n".getBytes("UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 拷贝文件/文件目录
     *
     * @param source 源文件目录
     * @param target 目标目录
     */
    private static void copyFiles(File source, File target) {
        copyFiles(source, target, "");
    }

    /**
     * 拷贝文件/文件目录
     *
     * @param source
     * @param target
     * @param linkPath
     */
    public static void copyFiles(File source, File target, String linkPath) {
        if (source.isDirectory()) {
            final String _linkPath = linkPath + File.separator + source.getName();
            asList(source.listFiles()).forEach(f -> {
                copyFiles(f, target, _linkPath);
            });
        } else if (source.isFile()) {
            try {
                String _linkPath = "";
                int index = linkPath.indexOf(File.separator, 1);
                if (index > 0) {
                    _linkPath = linkPath.substring(index);
                }
                File targetFile = new File(target.toPath() + _linkPath + File.separator + source.getName());
                if (!targetFile.getParentFile().exists()) {
                    targetFile.getParentFile().mkdirs();
                }

                Files.copy(source.toPath(), targetFile.toPath(), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取 clazz的路径，如果是jar里面的文件得到jar存放的目录，如：lib
     *
     * @param clazz
     * @return
     */
    public static String rootPath(Class clazz) {
        //return clazz.getClassLoader().getResource("").getPath();
        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        try {
            String filePath = URLDecoder.decode(url.getPath(), "utf-8");
            if (filePath.endsWith(".jar")) {
                return filePath.substring(0, filePath.lastIndexOf("/") + 1);
            }
            return filePath;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String rootPath() {
        return rootPath(FileKit.class);
    }

    /**
     * 读取流内的所有内容
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String readAll(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer buf = new StringBuffer();
        String str;
        try {
            while ((str = reader.readLine()) != null) {
                buf.append(str + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }

    public static <T> T readAs(File file, Type typeToken) throws IOException {
        try (
                FileInputStream inputStream = new FileInputStream(file)
        ) {
            return JsonConvert.root().convertFrom(typeToken, inputStream);
        }
    }

    /**
     * 渲染模板到文件
     *
     * @param sourceStr
     * @param target
     * @param kv
     */
    public static void tplRender(String sourceStr, File target, Kv kv) {
        String str = "";
        if (sourceStr != null && !sourceStr.isEmpty()) {
            str = Engine.use().getTemplateByString(sourceStr).renderToString(kv);
        }
        strToFile(str, target, true);
    }

    /**
     * 通过模板创建内容
     *
     * @param tplFile
     * @param para
     */
    public static void tplRender(File tplFile, File file, Map para) throws IOException {
        String str = Engine.use().getTemplate(tplFile.getPath()).renderToString(para);
        strToFile(str, file);
    }
}
