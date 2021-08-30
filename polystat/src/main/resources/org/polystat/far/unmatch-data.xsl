<?xml version="1.0"?>
<!--
The MIT License (MIT)

Copyright (c) 2020-2021 Polystat.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="unmatch-data" version="2.0">
  <!--
  This XSL will find attributes that are data and will put "N"
  into their TAU texts. For example, this XML:

  <o name="foo">
    <o line="3" name="a" data='42'>
      <opts>
         <opt m="xx" x="10">
            <tau i="4:1">1</tau>
            <tau i="3:2">1</tau>
         </opt>
      </opts>
    </o>
  </o>

  Will turn into (because 42 doesn't equal to 10):

  <o name="foo">
    <o line="3" name="a" data='42'>
      <opts>
         <opt m="xx" x="10">
            <tau i="4:1">N</tau>
            <tau i="3:2">N</tau>
         </opt>
      </opts>
    </o>
  </o>
  -->
  <xsl:strip-space elements="*"/>
  <xsl:param name="never"/>
  <xsl:template match="tau">
    <xsl:copy>
      <xsl:apply-templates select="@*|node() except text()"/>
      <xsl:variable name="opt" select="parent::opt"/>
      <xsl:choose>
        <xsl:when test="$opt/parent::opts/parent::o[@data and @data!=$opt/@x and $opt/@x!='\any']">
          <xsl:value-of select="$never"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="text()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
