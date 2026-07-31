package personal.limi.data.rules

data class RuleConfig(
    val UTMParams: Boolean,
    val UTMParamsEnhanced: Boolean,
    val bilibili: Boolean,
    val firefoxQueryStripping: Boolean,
    val braveCleanUrls: Boolean,
    val braveDebounce: Boolean,
    val braveQueryFilter: Boolean,
)
