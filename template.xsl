<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/clients">
        <html>
            <head>
                <meta charset="UTF-8"/>
                <title>Client report</title>
            </head>
            <body>
                <h1>Client report</h1>
                <table border="1">
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Contract date</th>
                        <th>Budget</th>
                    </tr>
                    <xsl:for-each select="client">
                        <tr>
                            <td><xsl:value-of select="@id"/></td>
                            <td><xsl:value-of select="name"/></td>
                            <td><xsl:value-of select="contractDate"/></td>
                            <td><xsl:value-of select="searchBudget"/></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
