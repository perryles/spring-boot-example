package com.plumroc.springbootuploadminio.dto;

import lombok.Data;

import java.io.Serializable;


/**
 * @author PlumRoc
 * @date 2022-11-12
 */
@Data
public class MinIoUploadResDTO implements Serializable {

    private static final long serialVersionUID = 475040120689218785L;
    private String minFileName;
    private String minFileUrl;

    public MinIoUploadResDTO(String minFileName, String minFileUrl) {
        this.minFileName = minFileName;
        this.minFileUrl = minFileUrl;
    }
}