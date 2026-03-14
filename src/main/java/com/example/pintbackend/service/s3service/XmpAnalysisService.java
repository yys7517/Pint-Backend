package com.example.pintbackend.service.s3service;

import com.example.pintbackend.dto.XmpAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class XmpAnalysisService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final String CRS_NS = "http://ns.adobe.com/camera-raw-settings/1.0/";
    private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    // Basic Adjustments key
    private static final List<String> BASIC_KEYS = List.of(
            "WhiteBalance", "Exposure2012", "Contrast2012", "Highlights2012", "Shadows2012",
            "Whites2012", "Blacks2012", "Texture", "Clarity2012", "Dehaze", "Vibrance", "Saturation"
    );

    // Color Adjustments Key
    private static final List<String> COLOR_KEYS = List.of(
            "HueAdjustmentRed", "HueAdjustmentOrange", "HueAdjustmentYellow", "HueAdjustmentGreen",
            "HueAdjustmentAqua", "HueAdjustmentBlue", "HueAdjustmentPurple", "HueAdjustmentMagenta",
            "SaturationAdjustmentRed", "SaturationAdjustmentOrange", "SaturationAdjustmentYellow",
            "SaturationAdjustmentGreen", "SaturationAdjustmentAqua", "SaturationAdjustmentBlue",
            "SaturationAdjustmentPurple", "SaturationAdjustmentMagenta",
            "LuminanceAdjustmentRed", "LuminanceAdjustmentOrange", "LuminanceAdjustmentYellow",
            "LuminanceAdjustmentGreen", "LuminanceAdjustmentAqua", "LuminanceAdjustmentBlue",
            "LuminanceAdjustmentPurple", "LuminanceAdjustmentMagenta"
    );

    // Detail Adjustment Key
    private static final List<String> DETAIL_KEYS = List.of(
            "Sharpness", "SharpenRadius", "SharpenDetail", "SharpenEdgeMasking",
            "LuminanceSmoothing", "LuminanceNoiseReductionDetail", "LuminanceNoiseReductionContrast",
            "ColorNoiseReduction", "ColorNoiseReductionDetail", "ColorNoiseReductionSmoothness"
    );

    private static final Set<String> META_KEYS = Set.of(
            "PresetType", "Cluster", "UUID", "RequiresRGBTables", "CameraModelRestriction",
            "Copyright", "ContactInfo", "Version", "ProcessVersion", "HasSettings"
    );

    /**
     * XMP 파일에서 실제로 변경된 보정값만 추출해 API 응답 형태로 변환한다.
     */
    public XmpAnalysisResponse analyze(MultipartFile xmpFile) throws IOException {
        if (xmpFile == null || xmpFile.isEmpty()) {
            throw new IllegalArgumentException("xmp 파일이 비어 있습니다.");
        }

        Element description = extractDescription(xmpFile.getInputStream()); // xmp 파일을 key-value 형태로 파싱
        Map<String, String> rawCrsSettings = filterChangedOnly(extractCrsAttributes(description));  // 기본 값 제외

        // basic, color, detail (기본 값 제외한)
        return new XmpAnalysisResponse(
                pick(rawCrsSettings, BASIC_KEYS),
                pick(rawCrsSettings, COLOR_KEYS),
                pick(rawCrsSettings, DETAIL_KEYS)
        );
    }

    /**
     * XMP 파일에서 실제로 변경된 보정값만 추출해 API 응답 형태로 변환한다.
     */

    public XmpAnalysisResponse analyze(String s3Key) throws IOException {
        if (s3Key == null || s3Key.isEmpty()) {
            throw new IllegalArgumentException("s3Key가 존재하지 않습니다.");
        }

//    InputStream xmpFileInputStream = amazonS3.getObject(bucket, s3Key).getObjectContent();

        InputStream xmpFileInputStream = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(s3Key)
                        .build()
        );

        if (xmpFileInputStream == null) {
            throw new IllegalArgumentException("xmp 파일이 존재하지 않습니다.");
        }

        Element description = extractDescription(xmpFileInputStream); // xmp 파일을 key-value 형태로 파싱
        Map<String, String> rawCrsSettings = filterChangedOnly(extractCrsAttributes(description));  // 기본 값 제외

        // basic, color, detail (기본 값 제외한)
        return new XmpAnalysisResponse(
                pick(rawCrsSettings, BASIC_KEYS),
                pick(rawCrsSettings, COLOR_KEYS),
                pick(rawCrsSettings, DETAIL_KEYS)
        );
    }

    /**
     * XMP XML을 안전하게 파싱하고, 핵심 설정이 담긴 rdf:Description 노드를 찾는다.
     * xmp 파일을 key-value 형태로 파싱
     */
    private Element extractDescription(InputStream inputStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            NodeList descriptions = doc.getElementsByTagNameNS(RDF_NS, "Description");
            if (descriptions.getLength() > 0) {
                return (Element) descriptions.item(0);
            }

            descriptions = doc.getElementsByTagName("rdf:Description");
            if (descriptions.getLength() > 0) {
                return (Element) descriptions.item(0);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("xmp 파일 파싱 실패: " + e.getMessage(), e);
        }

        throw new IllegalArgumentException("xmp에서 rdf:Description 노드를 찾지 못했습니다.");
    }

    /**
     * rdf:Description의 crs:* 속성(필터 속성)을 key-value 형태로 추출한다.
     */
    private Map<String, String> extractCrsAttributes(Element description) {
        Map<String, String> result = new LinkedHashMap<>();
        NamedNodeMap attrs = description.getAttributes();

        for (int i = 0; i < attrs.getLength(); i++) {
            Node node = attrs.item(i);
            if (!(node instanceof Attr attr)) {
                continue;
            }

            if (!CRS_NS.equals(attr.getNamespaceURI())) {
                continue;
            }

            String key = attr.getLocalName() != null ? attr.getLocalName() : attr.getName();
            result.put(key, attr.getValue());
        }

        return result;
    }

    /**
     * 특정 카테고리의 키 목록(Basic/Color/Detail)에 해당하는 key-value만 응답용으로 추린다.
     */
    private Map<String, String> pick(Map<String, String> source, List<String> keys) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String key : keys) {
            String value = source.get(key);
            if (value != null && !isDefaultValue(key, value)) {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 전체 crs 설정에서 기본값/메타값을 제거해 "실제 Custom 필터 값"만 가져온다.
     */
    private Map<String, String> filterChangedOnly(Map<String, String> source) {
        Map<String, String> result = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : source.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // 기본 값이 적용된 필터, 필터 메타정보 등을 제외
            if (isMetaKey(key) || isDefaultValue(key, value)) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }

    /**
     * 분석에 필요 없는 메타성 키인지 검사한다.
     */
    private boolean isMetaKey(String key) {
        return META_KEYS.contains(key) || key.startsWith("Supports");
    }

    /**
     * 기본값으로 간주할 수 있는 값인지 검사한다.
     */
    private boolean isDefaultValue(String key, String value) {
        if (value == null) {
            return true;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return true;
        }

        if ("false".equalsIgnoreCase(trimmed)) {
            return true;
        }

        if ("WhiteBalance".equals(key) && "As Shot".equalsIgnoreCase(trimmed)) {
            return true;
        }

        return isZeroNumber(trimmed);
    }

    /**
     * 0 계열 숫자(0, +0, 0.0 등)인지 검사한다.
     */
    private boolean isZeroNumber(String value) {
        if (!value.matches("^[+-]?\\d+(\\.\\d+)?$")) {
            return false;
        }

        try {
            return new BigDecimal(value).compareTo(BigDecimal.ZERO) == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
