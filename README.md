# IRI Generator
This project servers for generating IRIs using a predefined template based on existing RDF data.

## Use-cases

### Renaming temporary IRIs generated on the front-end to the proper ones
Having an RDF resource with an IRI:
```
@prefix demo: <http://demo.com/model#> .
http://resource/2 a demo:Two ; 
    demo:year "2021" ;
    demo:sequence "0005" .
```

created on the frontend, we want to generate the IRI `http://asquare.cogni.zone/2021/0005` based on:
- a template `#{[baseUri]}/#{[year]}/#{[sequence]}`
- a `baseUri` of `http://asquare.cogni.zone`
- a SPARQL query supplying the values for `year` and `sequence`
```
PREFIX demo: <http://demo.com/model#> .
SELECT ?year ?sequence {
  <#{[uri]}> demo:year ?year ;
  demo:sequence ?sequence
}
```
where `#{[uri]}` points to the original IRI of the resource (`http://resource/2`).

To pick the correct template for the given IRI another SPARQL query is used:
```
PREFIX demo: <http://demo.com/model#> .
SELECT ?uri { ?uri a demo:Two }
```

## Dependencies
The project has been extracted from [Asquare](https://github.com/cognizone/asquare) and the following classes are either copies of subset of their respective Asquare counterparts:
- [Json5Light.java](src%2Fmain%2Fjava%2Fzone%2Fcogni%2Fasquare%2Fcube%2Fjson5%2FJson5Light.java)
- [SpelService.java](src%2Fmain%2Fjava%2Fzone%2Fcogni%2Fasquare%2Fcube%2Fspel%2FSpelService.java)
- [TemplateService.java](src%2Fmain%2Fjava%2Fzone%2Fcogni%2Fasquare%2Fcube%2Fspel%2FTemplateService.java)