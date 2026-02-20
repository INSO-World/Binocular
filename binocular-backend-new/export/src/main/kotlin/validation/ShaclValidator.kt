package validation

import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.springframework.stereotype.Service
import org.topbraid.shacl.validation.ValidationUtil
import java.io.StringReader
import org.apache.jena.rdf.model.ModelFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.jena.rdf.model.Model
import org.topbraid.shacl.vocabulary.SH
import java.io.StringWriter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.inso_world.binocular.core.service.ShaclValidationPort
import com.inso_world.binocular.model.ShaclReport
import org.apache.jena.vocabulary.RDF

@Service
class ShaclValidator: ShaclValidationPort {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ShaclValidator::class.java)
        private const val SHACL_SHAPES_PATH = "branchExportShapeTestForGit.ttl"
        private const val CONTEXT_URL = "https://schemas.inso-world.com/binocular/v1/contextTestForGit.jsonld"
        private const val LOCAL_CONTEXT_PATH = "contextTestForGit.jsonld"

        // Load the context file content as a JsonNode containing only the definitions (no outer @context)
        private val localContextContent: ObjectNode by lazy {
            val inputStream = ShaclValidator::class.java.classLoader.getResourceAsStream(LOCAL_CONTEXT_PATH)
                ?: run {
                    logger.error("FATAL: Local context file not found at resource path: $LOCAL_CONTEXT_PATH")
                    throw IllegalStateException("Local context file not found!")
                }

            val objectMapper = ObjectMapper()

            // 1. Read the file stream into a JSON node
            val fileNode = objectMapper.readTree(inputStream)

            // 2. Extract the content of the top-level "@context" key
            val innerContext = fileNode.get("@context")

            if (innerContext !is ObjectNode) {
                logger.error("FATAL: Local context file is malformed or missing the inner context object.")
                throw IllegalStateException("Local context file is malformed.")
            }

            // 3. Store the actual inner context object for replacement
            innerContext
        }

        // Load the shapes model once when the class is initialized
        private val shapesModel: Model by lazy {
            val model = ModelFactory.createDefaultModel()
            val inputStream = ShaclValidator::class.java.classLoader.getResourceAsStream(SHACL_SHAPES_PATH)

            if (inputStream == null) {
                logger.error("FATAL: SHACL Shapes file not found at resource path: $SHACL_SHAPES_PATH")
                throw IllegalStateException("SHACL Shapes file not found!")
            }

            try {
                RDFDataMgr.read(model, inputStream, null, Lang.TTL)
            } catch (e: Exception) {
                logger.error("FATAL: Failed to read SHACL Shapes model from $SHACL_SHAPES_PATH: ${e.message}")
                throw IllegalStateException("SHACL initialization failed.")
            }
            model
        }
    }

    /**
     * Validates a JSON-LD data string against the predefined SHACL ruleset.
     */
    override fun validate(jsonLdString: String): ShaclReport {

        val shapes = shapesModel
        val dataModel = ModelFactory.createDefaultModel()

        val objectMapper = ObjectMapper()

        // 1. Parse the incoming JSON-LD string
        val jsonNode = try {
            objectMapper.readTree(jsonLdString)
        } catch (e: Exception) {
            logger.error("Failed to parse input JSON-LD string: ${e.message}")
            return ShaclReport(false, listOf("Malformed JSON: ${e.message}"))
        }

        // 2. Ensure it's a JSON object and retrieve the @context
        if (jsonNode !is ObjectNode || !jsonNode.has("@context")) {
            logger.error("Input JSON-LD is not a valid object or is missing the @context key.")
            return ShaclReport(false, listOf("Missing @context key in input."))
        }

        val contextNode = jsonNode.get("@context")

        // 3. Prepare the local context content (localContextContent is a String)
        val localContextJson = localContextContent

        // 4. Inject the local context content into the data graph's @context
        if (contextNode.isTextual && contextNode.asText() == CONTEXT_URL) {
            (jsonNode as ObjectNode).replace("@context", localContextJson)
        } else {
            logger.error("The @context value must be the remote URL string: $CONTEXT_URL for simple injection to work.")
            return ShaclReport(false, listOf("Invalid @context URL. Expected: $CONTEXT_URL"))
        }

        // 5. Convert the modified JSON structure back to a string for Jena
        val modifiedJsonLdString = objectMapper.writeValueAsString(jsonNode)
        // --- JSON INJECTION LOGIC END ---

        // 1. Load the Data Graph (the MODIFIED JSON-LD output)
        try {
            RDFDataMgr.read(dataModel, StringReader(modifiedJsonLdString), null, Lang.JSONLD)
        } catch (e: Exception) {
            logger.error("Failed to parse JSON-LD data into RDF model. JSON error: ${e.message}")
            return ShaclReport(false, listOf("RDF Parsing Error: ${e.message}"))
        }

        // 2. Run the validation
        val report = ValidationUtil.validateModel(dataModel, shapes, true)

        // 3. Check the results
        val conforms = report.getProperty(SH.conforms).boolean

        val rawRdfWriter = StringWriter()
        report.model.write(rawRdfWriter, "TURTLE")

        val criticalErrors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (!conforms) {
            val results = report.model.listStatements(null, RDF.type, SH.ValidationResult).toList()
            for (result in results) {
                val res = result.subject
                val message = res.getProperty(SH.resultMessage)?.string ?: "Constraint violation"
                val path = res.getProperty(SH.resultPath)?.resource?.localName ?: "Unknown property"

                val severity = res.getProperty(SH.resultSeverity)?.resource?.uri ?: SH.Violation.uri
                val formattedMsg = "[$path]: $message"

                if (severity == SH.Warning.uri) {
                    warnings.add(formattedMsg)
                } else {
                    criticalErrors.add(formattedMsg)
                }

                criticalErrors.add("[$path]: $message")
            }
        } else {
            logger.info("SHACL Validation Successful. Data conforms to the ruleset.")
        }

        return ShaclReport(conforms, criticalErrors, warnings, rawRdfWriter.toString())
    }
}
