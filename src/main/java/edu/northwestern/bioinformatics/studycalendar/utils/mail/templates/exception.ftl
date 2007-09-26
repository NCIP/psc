[#ftl]
There was an uncaught exception in the Study Calendar.

Stack trace:
${stackTrace}

[@displayMap mapName="request parameters"      mapEntries=requestParameters/]

[@displayMap mapName="request headers"         mapEntries=requestHeaders/]

[@displayMap mapName="cookies"                 mapEntries=cookies/]

[@displayMap mapName="request properties"      mapEntries=requestProperties/]

[@displayMap mapName="request attributes"      mapEntries=requestAttributes/]

[@displayMap mapName="session attributes"      mapEntries=sessionAttributes/]

[@displayMap mapName="application attributes"  mapEntries=applicationAttributes/]

[@displayMap mapName="context init parameters" mapEntries=initParameters/]

[#macro displayMapEntries entries]
    [#list entries as entry]
    ${entry.name}
        [#if entry.value?is_sequence]
            [#list entry.value as v]
        ${v}
            [/#list]
        [#else]
        ${entry.value}
        [/#if]
    [/#list]
[/#macro]

[#macro displayMap mapName mapEntries]
    [#if mapEntries?size gt 0]
${mapName?cap_first}:
[@displayMapEntries mapEntries/]
    [#else]
No ${mapName}
    [/#if]
[/#macro]
