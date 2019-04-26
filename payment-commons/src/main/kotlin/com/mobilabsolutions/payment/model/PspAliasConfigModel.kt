package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "PSP Alias Configuration")
data class PspAliasConfigModel(
    @ApiModelProperty(value = "Payment service provider type", example = "BS_PAYONE")
    val type: String?,

    @ApiModelProperty(value = "Merchant ID", example = "42865")
    val merchantId: String?,

    @ApiModelProperty(value = "Payment service provider mode", example = "test")
    val mode: String?,

    @ApiModelProperty(value = "Portal ID", example = "2030968")
    val portalId: String?,

    @ApiModelProperty(value = "Request name", example = "creditcardcheck")
    val request: String?,

    @ApiModelProperty(value = "API version", example = "3.11")
    val apiVersion: String?,

    @ApiModelProperty(value = "Response enum type", example = "JSON")
    val responseType: String?,

    @ApiModelProperty(value = "Encoding type", example = "UTF-8")
    val encoding: String?,

    @ApiModelProperty(value = "Hash", example = "35996f45100c40d51cffedcddc471f8189fc3568c287871568dc6c8bae1c4d732ded416b502f6191fb6085a2d767ef6f")
    val hash: String?,

    @ApiModelProperty(value = "Account ID", example = "42949")
    val accountId: String?,

    @ApiModelProperty(value = "Public key", example = "bbdjshcjdhdsgf")
    val publicKey: String?,

    @ApiModelProperty(value = "Private key", example = "ncbcjdheufhdhfjh")
    val privateKey: String?,

    @ApiModelProperty(value = "Client token", example = "eyJ2ZXJzaW9uIjoyLCJhdXRob3JpemF0aW9uRmluZ2VycHJpbnQiOiJk")
    val clientToken: String?,

    @ApiModelProperty(value = "PSP Payment Session", example = "eyJjaGVja291dHNob3BwZXJCYXNlVXJsIjoiaHR0cHM6XC9cL2NoZWNrb3V0c2hvcHBlci10ZXN0LmFkeWVuLmNvbVwvY2hlY2tvdXRzaG9wcGVyXC8iLCJkaXNhYmxlUmVjdXJyaW5nRGV0YWlsVXJsIjoiaHR0cHM6XC9cL2NoZWNrb3V0c2hvcHBlci10ZXN0LmFkeWVuLmNvbVwvY2hlY2tvdXRzaG9wcGVyXC9zZXJ2aWNlc1wvUGF5bWVudEluaXRpYXRpb25cL3YxXC9kaXNhYmxlUmVjdXJyaW5nRGV0YWlsIiwiZ2VuZXJhdGlvbnRpbWUiOiIyMDE5LTA0LTIzVDEzOjI3OjU0WiIsImluaXRpYXRpb25VcmwiOiJodHRwczpcL1wvY2hlY2tvdXRzaG9wcGVyLXRlc3QuYWR5ZW4uY29tXC9jaGVja291dHNob3BwZXJcL3NlcnZpY2VzXC9QYXltZW50SW5pdGlhdGlvblwvdjFcL2luaXRpYXRlIiwicGF5bWVudCI6eyJhbW91bnQiOnsiY3VycmVuY3kiOiJFVVIiLCJ2YWx1ZSI6MH0sImNvdW50cnlDb2RlIjoiREUiLCJyZWZlcmVuY2UiOiJBa3BsaSIsInJldHVyblVybCI6ImFwcDpcL1wvIiwic2Vzc2lvblZhbGlkaXR5IjoiMjAxOS0wNC0yM1QxMzoyNzo1NC41OTQ5OTU5MDBaIiwic2hvcHBlckxvY2FsZSI6ImRlX0RFIiwic2hvcHBlclJlZmVyZW5jZSI6ImZycjdBbnZzSmJkWUlVOUVlN1VLIn0sInBheW1lbnREYXRhIjoiQWIwMmI0YzAhQlFBQkFnQ0Y2amNuSGlsY01lSGhhejgrWTZvZTRiVHZRelh4VStPUXlcL1hpbTdiQ1VsNXU2QVNEV3luTk9zbGpQc0lZVG0rZ2xDSldNcjFHZVFjZURGcnFrcFBLbUg4Y0M1VWMyVnhBWFZSczVcL1BJdWlsMW5DUG1ySkRNeTFpcEI5VVQ3VlA4S1pPMGFzVXF1eWg2S2ttN09LQVV5OW9UbEtpNXQ1KzBQemFxYmQzclV2ZFEwTUN6cnE5akpaZ0E3V2ExRjU1aVUzd2x0XC81dDh6dmF1YXV4WUxxTDQ2Wmd5a3IwTzVHcTE2REJoVzcyNlgySG5nYWlnb2hjanhna215WXlrSUNKRnFTWGo0emk0NFwvcjhwdjlyZG82cGZ0WThjWGIxRFR5SkNiZkNGNmEyTWk5Z1JvajQ4S2RHYnBjcUdYU0p0OHpxaHJvNmtDRUJhemp3T3k4RHRFZ0lFTW5CUXBqbG1OcTExdEVXSXBzaXJNN25zQ0p2dmYrbmZ3XC9rYlNaM0ZcL01aenlMcWJtcnQrcGRZUHhWVWNybE9KOFwvR3k4R0IyUlk1cWlUVVhnYllIQU5ERWFrcTNOdHdhUGdwV3ZTXC8rS1ZKTncwQXJTTlBGSFpDMW83SGlcL2VzaUtcL3FBMFR6anI4YUduSnFZRGgxdjh1NTFZOWtNeWl6bWdxbjVZTE05YU5mbitNcTFod2EwNHVxRzhvcDBQK0JcL0tmT1VjcWJ4cUpOaGc3TzVVSndoZjRXZ2dYYmtjTktyOEM3cnU2RlZjbFBMK25RTjVDUU5kZzRaWEVRcmFLVDg2N25tTWJNc1JIbER2VGZkN2hcL0NRNHM3Qmoza1RMZHllY3JsdmNQTUh6TUljVmNpT0hhSXhCZnhpU2FWNldvNENuK1wvTU1cL1ZLeVZoR3huWkF3UFJESDRVOTZzN2JaRyt5Zm9OcUh2WEJQQUVwN0ltdGxlU0k2SWtGR01FRkJRVEV3TTBOQk5UTTNSVUZGUkRnM1F6STBSRVExTXprd09VSTRNRUUzT0VFNU1qTkZNemd5TTBRMk9FUkJRME01TkVJNVJrWTRNekExUkVNaWZYKzZFbHBFdVc1andtSEdHQldFYzFwMG12VGNCWTVPWXhwdWVZa2g0cnBHYjNndVhjQUpWR0xkd0QyREZ6VElUajMzc3FrdFFpY002TEEzbXRzNnBSTTNOODIrVWpjY1kwV2FsZW1VNjRtUGJBK2hjdlJcLzBDN0k4VWFwdlk2RFFFZFFyN3lGVjFvNXYxMkJwYTJKZnRpRzZmcDFtXC9oa1hyODlkaWhVa1FNVUptZHhwVFVhSVZydCtlcVRcL001QXQ3SUl1UytYVm9hb0FRSDYzN3FyNzBFUlhrYk1Ib1drb0xsOERYZ3F5M1ExQ1hRRmd3MGQ0bEIxUUc3eTdncFZ4UzlYbmpQUjRpNUh6WndYM2hkOEo4b3B2SWZlWXYzV29hRDFVc2NoVGd5U1JnclJqWklDZlhXZzUzZW15NVVUTktLR0RDZ1wvSzA4QkZKenJuU25TSktDbHhUcW1yUnMrWWErWTV3a1ZvbmpSOVlBUVpIb3U1SjJ4V2NkVWQydFdJV2JSSnNJbU1qS")
    val paymentSession: String?
)
