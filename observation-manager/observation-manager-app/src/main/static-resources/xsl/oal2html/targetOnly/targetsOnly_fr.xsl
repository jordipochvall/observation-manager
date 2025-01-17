<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:external="http://ExternalFunction.xalan-c++.xml.apache.org" exclude-result-prefixes="external">

	<!-- Formatting Angle -->

	<xsl:template name="angle">

		<xsl:param name="angle"/>

		<xsl:value-of select="$angle"/>

		<xsl:choose>

			<xsl:when test="$angle[@unit='arcsec']">&#8221;</xsl:when>

			<xsl:when test="$angle[@unit='arcmin']">&#8242;</xsl:when>

			<xsl:when test="$angle[@unit='deg']">&#176;</xsl:when>

			<xsl:when test="$angle[@unit='rad']"> rad</xsl:when>

		</xsl:choose>

	</xsl:template>

	

	<xsl:template match="target">
		
			<table border="0" cellspacing="3" cellpadding="3" width="90%" style="font-size:14;font-family:Verdana,Arial">
					
				<tr>

					<td valign="top" width="20%">

						<xsl:if test="count(constellation)>0">
							<b>Constellation:<br/></b><ul><li type="square"><xsl:value-of select="constellation"/></li></ul>
						</xsl:if>
						<xsl:if test="count(constellation)=0">
							<b>Pas de constellation</b>
						</xsl:if>
												
					</td>

					<td width="80%"></td>

				</tr>

				<tr>

					<td width="20%"></td>

					<td valign="top" width="80%" halign="left">

						<table border="1" cellspacing="0" cellpadding="2" width="60%" style="font-size:14;font-family:Verdana,Arial">

							<tr>

								<td>
									<b>Objet: </b>

									<xsl:choose>

										<xsl:when test="@type='oal:PlanetTargetType' or @type='oal:MoonTargetType' or  @type='oal:SunTargetType'">

											<xsl:choose>
												<xsl:when test="name='SUN'">Soleil</xsl:when>
												<xsl:when test="name='MERCURY'">Mercure</xsl:when>
												<xsl:when test="name='VENUS'">Vénus</xsl:when>
												<xsl:when test="name='EARTH'">Terre</xsl:when>
												<xsl:when test="name='MOON'">Lune</xsl:when>
												<xsl:when test="name='MARS'">Mars</xsl:when>
												<xsl:when test="name='JUPITER'">Jupiter</xsl:when>
												<xsl:when test="name='SATURN'">Saturne</xsl:when>
												<xsl:when test="name='URANUS'">Uranus</xsl:when>
												<xsl:when test="name='NEPTUNE'">Neptune</xsl:when>

												<xsl:otherwise>
													<xsl:value-of select="name"/>
												</xsl:otherwise>

											</xsl:choose>

										</xsl:when>

										<xsl:otherwise>
											<xsl:value-of select="name"/>
										</xsl:otherwise>

									</xsl:choose>

								</td>

							</tr>

							<tr>

								<td>
									<b>Type: </b>
									
									<xsl:choose>

										<xsl:when test="@type='oal:deepSkyGX'">Galaxie</xsl:when>
										<xsl:when test="@type='oal:deepSkyGC'">Amas globulaire</xsl:when>
										<xsl:when test="@type='oal:deepSkyGN'">Nébuleuse diffuse</xsl:when>
										<xsl:when test="@type='oal:deepSkyOC'">Amas ouvert</xsl:when>
										<xsl:when test="@type='oal:deepSkyPN'">Nébuleuse planétaire</xsl:when>
										<xsl:when test="@type='oal:deepSkyQS'">Quasar</xsl:when>
										<xsl:when test="@type='oal:deepSkyDS'">Étoile double</xsl:when>
										<xsl:when test="@type='oal:deepSkyDN'">Nébuleuse obscure</xsl:when>
										<xsl:when test="@type='oal:deepSkyAS'">Astérisme</xsl:when>
										<xsl:when test="@type='oal:deepSkySC'">Nuage stellaire</xsl:when>
										<xsl:when test="@type='oal:deepSkyMS'">Système d'étoiles multiples</xsl:when>
										<xsl:when test="@type='oal:deepSkyCG'">Amas de galaxies</xsl:when>
										<xsl:when test="@type='oal:variableStarTargetType'">Étoile variable</xsl:when>
										<xsl:when test="@type='oal:SunTargetType'">Soleil</xsl:when>
										<xsl:when test="@type='oal:MoonTargetType'">Lune</xsl:when>
										<xsl:when test="@type='oal:PlanetTargetType'">Planète</xsl:when>
										<xsl:when test="@type='oal:MinorPlanetTargetType'">Objet mineur</xsl:when>
										<xsl:when test="@type='oal:CometTargetType'">Comète</xsl:when>
										<xsl:when test="@type='oal:UndefinedTargetType'">(autre objet)</xsl:when>

										<xsl:otherwise>(Type inconnu)</xsl:otherwise>

									</xsl:choose>

								</td>

							</tr>

							<xsl:if test="count(alias)>0">

								<tr>

									<td>
										<b>Alias: </b>
										<xsl:for-each select="alias">

											<xsl:value-of select="."/>

											<xsl:if test="position() != last()">, </xsl:if>

										</xsl:for-each>

									</td>

								</tr>

							</xsl:if>

							<tr>

								<td>
									<b>Position: </b>

									<xsl:if test="boolean(position/ra)">

										<table border="0" cellspacing="0" cellpadding="2" style="font-size:12;font-family:Verdana, Arial">

											<tr>

												<td>AD: </td>

												<td>

													<xsl:call-template name="formatHHMM">

														<xsl:with-param name="node" select="position/ra"/>

													</xsl:call-template>

												</td>

											</tr>

										</table>

									</xsl:if>

									<xsl:if test="boolean(position/dec)">

										<table border="0" cellspacing="0" cellpadding="2" style="font-size:12;font-family:Verdana, Arial">

											<tr>

												<td>Dec: </td>

												<td>

													<xsl:call-template name="formatDDMM">

														<xsl:with-param name="node" select="position/dec"/>

													</xsl:call-template>

												</td>

											</tr>

										</table>

									</xsl:if>

								</td>

							</tr>

							<!-- Output from attributes of Subclasses -->
							<xsl:if test="contains(@type,'oal:deepSky')">
								<!-- Deep Sky -->
								<xsl:if test="boolean(smallDiameter) and boolean(largeDiameter)">
									<tr>
										<td>
											<b>Dimensions: </b>
											<xsl:call-template name="angle">
												<xsl:with-param name="angle" select="smallDiameter"/>
											</xsl:call-template> &#215;
											<xsl:call-template name="angle">
												<xsl:with-param name="angle" select="largeDiameter"/>
											</xsl:call-template>
										</td>
									</tr>
								</xsl:if>
								<xsl:if test="boolean(visMag)">
									<tr>
										<td>
											<b>Magnitude (v): </b> <xsl:value-of select="visMag"/> mag</td>
									</tr>
								</xsl:if>
								<xsl:if test="boolean(surfBr)">
									<tr>
										<td><b>Magnitude surfacique:</b>	<xsl:value-of select="surfBr"/> mag/arcmin²</td>
									</tr>
								</xsl:if>
							</xsl:if>
							<xsl:if test="boolean(observer)">
								<tr>
									<td>
										<b>Source: </b>
										<xsl:text disable-output-escaping="yes">&lt;a href="#observer</xsl:text>
										<xsl:value-of select="observer"/>
										<xsl:text disable-output-escaping="yes">"&gt;</xsl:text>
										<xsl:value-of select="key('observerKey', observer)/surname"/>,
										<xsl:text/>
										<xsl:value-of select="key('observerKey', observer)/name"/>
										<xsl:text disable-output-escaping="yes">&lt;/a&gt;</xsl:text>
									</td>
								</tr>
							</xsl:if>
							<xsl:if test="boolean(datasource)">
								<tr>
									<td>
										<b>Source: </b><xsl:value-of select="datasource"/></td>
								</tr>
							</xsl:if>							

						</table>

					</td>

				</tr>
					
			</table>		

	</xsl:template>

	

	<xsl:template name="formatHHMM">

		<xsl:param name="node"/>

                

		<xsl:param name="hrs"><xsl:value-of select='floor($node div 15)'/></xsl:param>

		<xsl:param name="hrs_rest"><xsl:value-of select='$node - ($hrs * 15)'/></xsl:param>

		<xsl:param name="minutes"><xsl:value-of select='floor($hrs_rest * 60 div 15)'/></xsl:param>

		<xsl:param name="minutes_rest"><xsl:value-of select='$hrs_rest - ($minutes div 60 * 15)'/></xsl:param>

                <xsl:param name="sec"><xsl:value-of select='round($minutes_rest * 3600 div 15)'/></xsl:param>

		<result><xsl:value-of select="$hrs"/>h <xsl:if test="$minutes &lt; 10">0</xsl:if><xsl:value-of select="$minutes"/>mn <xsl:if test="$sec  &lt; 10">0</xsl:if><xsl:value-of select="$sec"/>s</result>

	</xsl:template>

	

	

	<xsl:template name="formatDDMM">

		<xsl:param name="node"/>

		<xsl:if test='$node &lt; 0'> 

			<xsl:call-template name="formatDDMM_lower">

				<xsl:with-param name="node" select="$node"/>

			</xsl:call-template>

		</xsl:if>

		<xsl:if test="$node &gt; 0 or $node = 0">

			<xsl:call-template name="formatDDMM_higher">

				<xsl:with-param name="node" select="$node"/>

			</xsl:call-template>

		</xsl:if> 

	</xsl:template>

	

        

	<xsl:template name="formatDDMM_lower">

		<xsl:param name="node"/>



		<xsl:param name="abs_degrees"><xsl:value-of select='- $node'/></xsl:param>

		<xsl:param name="degs"><xsl:value-of select='floor($abs_degrees)'/></xsl:param>				

		<xsl:param name="degs_rest"><xsl:value-of select='$abs_degrees -  $degs'/></xsl:param>

		

		<xsl:param name="minutes"><xsl:value-of select='floor(60 * ($degs_rest))'/></xsl:param>

		<xsl:param name="minutes_rest"><xsl:value-of select='$degs_rest - ($minutes div 60)'/></xsl:param>

		

		<xsl:param name="sec"><xsl:value-of select='round($minutes_rest * 3600)'/></xsl:param>

		

		<result>-<xsl:value-of select="$degs"/><xsl:text>&#176; </xsl:text><xsl:if test="$minutes &lt; 10">0</xsl:if><xsl:value-of select="$minutes"/><xsl:text>&apos; </xsl:text><xsl:if test="$sec &lt; 10">0</xsl:if><xsl:value-of select="$sec"/><xsl:text>&quot;</xsl:text></result>

	</xsl:template>

        

        

	<xsl:template name="formatDDMM_higher">

		<xsl:param name="node"/>



		<xsl:param name="degs"><xsl:value-of select='floor($node)'/></xsl:param>				

		<xsl:param name="degs_rest"><xsl:value-of select='$node -  $degs'/></xsl:param>

		

		<xsl:param name="minutes"><xsl:value-of select='floor(60 * ($degs_rest))'/></xsl:param>

		<xsl:param name="minutes_rest"><xsl:value-of select='$degs_rest - ($minutes div 60)'/></xsl:param>

		

		<xsl:param name="sec"><xsl:value-of select='round($minutes_rest * 3600)'/></xsl:param>

		

		<result><xsl:value-of select="$degs"/><xsl:text>&#176; </xsl:text><xsl:if test="$minutes &lt; 10">0</xsl:if><xsl:value-of select="$minutes"/><xsl:text>&apos; </xsl:text><xsl:if test="$sec &lt; 10">0</xsl:if><xsl:value-of select="$sec"/><xsl:text>&quot;</xsl:text></result>

	</xsl:template> 	

        

	<xsl:template match="observer">

		<p>

			<xsl:text disable-output-escaping="yes">&lt;a name="observer</xsl:text>

			<xsl:value-of select="@id"/>

			<xsl:text disable-output-escaping="yes">"&gt;</xsl:text>

			<b>Observateur: </b>

			<xsl:value-of select="name"/>

			<xsl:text> </xsl:text>

		     <xsl:value-of select="surname"/>

			<xsl:text disable-output-escaping="yes">&lt;/a&gt;</xsl:text>

		</p>

		<xsl:if test="count(contact) > 0">Contacts: <br/>

			<ul>

				<xsl:for-each select="contact">

					<li>

						<xsl:value-of select="."/>

					</li>

				</xsl:for-each>

			</ul>

		</xsl:if>

		<xsl:call-template name="linkTop"/>

	</xsl:template>

	

	<xsl:output method="html"/>

	<!-- mit diesen Schluesselelementen loesen wir die Referenzen komfortabel wieder auf :-) -->

	<xsl:key name="targetKey" match="targets/target" use="@id"/>

	<xsl:key name="observerKey" match="observers/observer" use="@id"/>

	<xsl:template match="/">

		<HTML>

			<HEAD>

				<TITLE>Liste d'objets à observer</TITLE>

			</HEAD>

			<BODY>

				<div align="center" style="font-size:24;font-family:Verdana,Arial;color:#0000C0">Liste d'objets à observer</div>

				<div style="font-size:12;font-family:Verdana, Arial">

					<!-- Beobachtungen in Dokumentenreihenfolge ausgeben -->

					<a name="objectList"/>

					<h3>Objets</h3>

					<xsl:for-each select="//targets/target">
						<xsl:sort select="constellation"/>
						<xsl:apply-templates select="."/>
					</xsl:for-each>										

					<!-- Stammdaten ausgeben -->

					<h3>Références</h3>



					<!-- Stammdaten der Beobachter ausgeben -->

					<xsl:for-each select="//observers/observer">

						<xsl:sort select="name"/>

						<xsl:sort select="surname"/>

						<xsl:apply-templates select="."/>

					</xsl:for-each>



					<!-- Generierungsdatum ausgeben -->

					<script type="text/javascript">

						<xsl:text disable-output-escaping="yes">

                  &#60;!--

                  document.write("Créé le " + document.lastModified.replace(/(\d{2})\/(\d{2})/,"$2/$1"));

                  //--&#62;

               </xsl:text>
               
					</script>
					<xsl:text disable-output-escaping="yes">&lt;br&gt;avec &lt;a href="http://observation.sourceforge.net/en/" target="_blank"&gt;Observation Manager&lt;/a&gt;</xsl:text>

				</div> 

			</BODY>

		</HTML>

	</xsl:template>		

	

	<!-- Link zurueck zur Liste der Beobachtungen -->

	<xsl:template name="linkTop">

		<xsl:text disable-output-escaping="yes">&lt;a href="#objectList"&gt; &gt;&gt; Liste d'objets &lt;&lt;&lt;/a&gt;</xsl:text>

		<hr/>

	</xsl:template>

</xsl:stylesheet>
