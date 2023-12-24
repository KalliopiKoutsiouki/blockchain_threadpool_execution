package com.warehouse.model;

import com.google.gson.Gson;
import jakarta.persistence.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.lang.NonNull;

@Entity
@Table(name="PRODUCTS")
public final class ProductBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name="PRODUCT_BLOCK",columnDefinition = "LONGTEXT", nullable=false, unique=true)
    @NonNull
    private String blockJson;

    public ProductBlock() {

    }
    public ProductBlock( @NonNull String blockJson) {
        this.blockJson = blockJson;
    }

    public String getBlockJson() {
        return blockJson;
    }

    public BlockDto extractBlockDto () {
        return new Gson().fromJson(this.removeQuotesAndUnescape(this.getBlockJson()), BlockDto.class);
    }

    public void setBlockJson(@NonNull String blockJson) {
        this.blockJson = blockJson;
    }

    private String removeQuotesAndUnescape(String uncleanJson) {
        String noQuotes = uncleanJson.replaceAll("^\"|\"$", "");
        return StringEscapeUtils.unescapeJava(noQuotes);
    }
}
