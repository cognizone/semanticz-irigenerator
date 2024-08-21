# IRI Generator
This project servers for generating IRIs using a predefined template based on existing RDF data.

## Use-cases

### Renaming temporary IRIs generated on the front-end to the proper ones
Having an RDF resource with an IRI:
```
@prefix eczm: <https://example.cogni.zone/model#> .
https://resource/2 a eczm:Two ; 
    eczm:year "2021" ;
    eczm:sequence "0005" .
```

created on the frontend, we want to generate the IRI `https://example.cogni.zone/2021/0005` based on:
- a template `#{[baseUri]}/#{[year]}/#{[sequence]}`
- a `baseUri` of `https://example.cogni.zone`
- a SPARQL query supplying the values for `year` and `sequence`
```
PREFIX eczm: <https://example.cogni.zone/model#> 
SELECT ?year ?sequence {
  <#{[uri]}> eczm:year ?year ;
  eczm:sequence ?sequence
}
```
where `#{[uri]}` points to the original IRI of the resource (`https://resource/2`).

To pick the correct template for the given IRI another SPARQL query is used:
```
PREFIX eczm: <https://example.cogni.zone/model#> .
SELECT ?uri { ?uri a eczm:Two }
```

## Dependencies
The project has been extracted from [Asquare](https://github.com/cognizone/asquare) and the following classes are either copies of subset of their respective Asquare counterparts:
- [Json5Light.java](src%2Fmain%2Fjava%2Fzone%2Fcogni%2Fasquare%2Fcube%2Fjson5%2FJson5Light.java)
- [SpelService.java](src%2Fmain%2Fjava%2Fzone%2Fcogni%2Fasquare%2Fcube%2Fspel%2FSpelService.java)
- [TemplateService.java](src%2Fmain%2Fjava%2Fzone%2Fcogni%2Fasquare%2Fcube%2Fspel%2FTemplateService.java)
- [InternalRdfStoreService.java](src%2Fmain%2Fjava%2Fzone%2Fcogni%2Fasquare%2Ftriplestore%2FInternalRdfStoreService.java)
- [RdfStoreServiceAPI.java](src%2Fmain%2Fjava%2Fzone%2Fcogni%2Fasquare%2Ftriplestore%2FRdfStoreServiceAPI.java)
- [JenaQueryUtils.java](src%2Fmain%2Fjava%2Fzone%2Fcogni%2Fsem%2Fjena%2Ftemplate%2FJenaQueryUtils.java)
- [JenaResultSetHandler.java](src%2Fmain%2Fjava%2Fzone%2Fcogni%2Fsem%2Fjena%2Ftemplate%2FJenaResultSetHandler.java)