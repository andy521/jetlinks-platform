package org.jetlinks.platform.manager.excel;


import java.io.OutputStream;
import java.util.Map;
import java.util.function.Function;

/**
 * @author bsetfeng
 * @since 1.0
 **/
public interface ImportExportService {


    <T> void doImport(String fileUrl, Class<T> type, Map<String, String> headerNameMapper, Function<T, Boolean> consumerData);


    void writeImportExcelTemplate(OutputStream outputStream, Map<String, String> header);

}
