package cn.xryder.base.common;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文件读取工具类
 *
 * @author wrm244
 */
@Component
public class FileReader {

    // 将单元格的值转换为字符串
    private static String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // 如果是日期，返回日期字符串
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理长整型数字，避免科学计数法
                    BigDecimal bigDecimalValue = BigDecimal.valueOf(cell.getNumericCellValue());
                    // 转换为不使用科学计数法的字符串
                    return bigDecimalValue.toPlainString();
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "Unknown Cell Type";
        }
    }

    // 读取TXT文件
    public String readTxtFile(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    // 读取CSV文件
    public String readCsvFile(MultipartFile file) throws IOException, CsvException {
        StringBuilder content = new StringBuilder();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> rows = csvReader.readAll();
            for (String[] row : rows) {
                content.append(String.join(", ", row)).append("\n");
            }
        }
        return content.toString();
    }

    // 读取Excel文件
    public String readExcelFile(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    content.append(getCellValueAsString(cell)).append("\t");
                }
                content.append("\n");
            }
        }
        return content.toString();
    }

    // 读取Docx文件
    public String readDocxFile(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            document.getParagraphs().forEach(paragraph -> content.append(paragraph.getText()).append("\n"));
        }
        return content.toString();
    }

    // 读取PDF文件
    public String readPdfFile(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (PDDocument document = Loader.loadPDF((RandomAccessRead) file.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            content.append(pdfStripper.getText(document));
        }
        return content.toString();
    }
}
