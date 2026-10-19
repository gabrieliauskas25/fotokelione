// Bendri sluoksniai, iššokantys langai ir paieškos šaltiniai 2D ir 3D darbams.
define([
  "esri/Graphic",
  "esri/layers/FeatureLayer",
  "esri/widgets/Search"
], function (Graphic, FeatureLayer, Search) {
  const D = window.LT_DATA;

  // Geodezinis atstumas (haversino formulė), km
  function atstumasKm(lon1, lat1, lon2, lat2) {
    const R = 6371.0088;
    const rad = Math.PI / 180;
    const dLat = (lat2 - lat1) * rad;
    const dLon = (lon2 - lon1) * rad;
    const a = Math.sin(dLat / 2) ** 2 +
      Math.cos(lat1 * rad) * Math.cos(lat2 * rad) * Math.sin(dLon / 2) ** 2;
    return 2 * R * Math.asin(Math.sqrt(a));
  }

  const TIPU_SPALVOS = {
    "Didmiestis": "#c0392b",
    "Apskrities centras": "#e67e22",
    "Kurortas": "#16a085",
    "Miestas": "#2c6fbb"
  };

  const VIETU_SPALVOS = {
    "Pilis": "#8e44ad",
    "Šventovė": "#d35400",
    "UNESCO": "#b7950b",
    "Muziejus": "#2471a3",
    "Gamta": "#1e8449",
    "Kita": "#566573"
  };

  // ---------- Miestai ----------
  function miestuGrafikos() {
    return D.miestai.map(function (m, i) {
      return new Graphic({
        geometry: { type: "point", longitude: m[4], latitude: m[5] },
        attributes: {
          ObjectID: i + 1,
          pavadinimas: m[0],
          apskritis: m[1] + " apskritis",
          tipas: m[2],
          gyventojai: m[3],
          atstumas: Math.round(atstumasKm(D.sostine.lon, D.sostine.lat, m[4], m[5]))
        }
      });
    });
  }

  const miestuPopup = {
    title: "{pavadinimas}",
    content: [
      {
        type: "fields",
        fieldInfos: [
          { fieldName: "apskritis", label: "Apskritis" },
          { fieldName: "tipas", label: "Tipas" },
          { fieldName: "gyventojai", label: "Gyventojai", format: { digitSeparator: true, places: 0 } },
          { fieldName: "atstumas", label: "Atstumas iki Vilniaus, km" },
          { fieldName: "expression/dalis", label: "Dalis nuo Lietuvos gyventojų" }
        ]
      }
    ],
    expressionInfos: [
      {
        name: "dalis",
        title: "Dalis nuo Lietuvos gyventojų",
        expression: "Text($feature.gyventojai / 2890000 * 100, '#0.00') + ' %'"
      }
    ]
  };

  function tipoRenderer() {
    return {
      type: "unique-value",
      field: "tipas",
      legendOptions: { title: "Miesto tipas" },
      uniqueValueInfos: Object.keys(TIPU_SPALVOS).map(function (t) {
        return {
          value: t,
          label: t,
          symbol: {
            type: "simple-marker",
            style: t === "Kurortas" ? "diamond" : "circle",
            size: t === "Didmiestis" ? 14 : t === "Miestas" ? 7 : 10,
            color: TIPU_SPALVOS[t],
            outline: { color: "white", width: 1 }
          }
        };
      })
    };
  }

  function sukurtiMiestus(extra) {
    return new FeatureLayer(Object.assign({
      title: "Lietuvos miestai",
      source: miestuGrafikos(),
      objectIdField: "ObjectID",
      geometryType: "point",
      spatialReference: { wkid: 4326 },
      fields: [
        { name: "ObjectID", type: "oid" },
        { name: "pavadinimas", alias: "Pavadinimas", type: "string" },
        { name: "apskritis", alias: "Apskritis", type: "string" },
        { name: "tipas", alias: "Tipas", type: "string" },
        { name: "gyventojai", alias: "Gyventojai", type: "integer" },
        { name: "atstumas", alias: "Atstumas iki Vilniaus, km", type: "integer" }
      ],
      outFields: ["*"],
      popupTemplate: miestuPopup,
      renderer: tipoRenderer(),
      labelingInfo: [{
        labelExpressionInfo: { expression: "$feature.pavadinimas" },
        where: "gyventojai >= 20000 OR tipas = 'Kurortas'",
        symbol: {
          type: "text",
          color: "#1d1d1d",
          haloColor: "white",
          haloSize: 1.5,
          font: { size: 10, weight: "bold", family: "Noto Sans" }
        },
        labelPlacement: "above-center"
      }]
    }, extra || {}));
  }

  // ---------- Lankytinos vietos ----------
  function sukurtiVietas(extra) {
    const grafikos = D.vietos.map(function (v, i) {
      return new Graphic({
        geometry: { type: "point", longitude: v[4], latitude: v[5] },
        attributes: {
          ObjectID: i + 1,
          pavadinimas: v[0],
          kategorija: v[1],
          vietove: v[2],
          aprasymas: v[3]
        }
      });
    });

    return new FeatureLayer(Object.assign({
      title: "Lankytinos vietos",
      source: grafikos,
      objectIdField: "ObjectID",
      geometryType: "point",
      spatialReference: { wkid: 4326 },
      fields: [
        { name: "ObjectID", type: "oid" },
        { name: "pavadinimas", alias: "Pavadinimas", type: "string" },
        { name: "kategorija", alias: "Kategorija", type: "string" },
        { name: "vietove", alias: "Vietovė", type: "string" },
        { name: "aprasymas", alias: "Aprašymas", type: "string" }
      ],
      outFields: ["*"],
      popupTemplate: {
        title: "{pavadinimas}",
        content: "<b>{kategorija}</b> · {vietove}<br>{aprasymas}"
      },
      renderer: {
        type: "unique-value",
        field: "kategorija",
        legendOptions: { title: "Lankytinos vietos" },
        uniqueValueInfos: Object.keys(VIETU_SPALVOS).map(function (k) {
          return {
            value: k,
            label: k,
            symbol: {
              type: "simple-marker",
              style: "square",
              size: 9,
              color: VIETU_SPALVOS[k],
              outline: { color: "white", width: 1 }
            }
          };
        })
      }
    }, extra || {}));
  }

  // ---------- Apskritys (Esri Living Atlas, filtruota Lietuvai) ----------
  function sukurtiApskritis(extra) {
    return new FeatureLayer(Object.assign({
      title: "Lietuvos apskritys",
      url: "https://services.arcgis.com/P3ePLMYs2RVChkJx/arcgis/rest/services/World_Administrative_Divisions/FeatureServer/0",
      definitionExpression: "COUNTRY = 'Lithuania'",
      outFields: ["NAME", "ISO_CODE"],
      opacity: 0.9,
      renderer: {
        type: "simple",
        symbol: {
          type: "simple-fill",
          color: [255, 214, 0, 0.06],
          outline: { color: [0, 106, 68, 0.9], width: 1.5 }
        }
      },
      popupTemplate: {
        title: "{NAME}",
        content: [{
          type: "fields",
          fieldInfos: [
            { fieldName: "ISO_CODE", label: "ISO kodas" },
            { fieldName: "expression/plotas", label: "Plotas, km²" }
          ]
        }],
        expressionInfos: [{
          name: "plotas",
          expression: "Text(AreaGeodetic($feature, 'square-kilometers'), '#,###')"
        }]
      }
    }, extra || {}));
  }

  // ---------- Paieška su keliais šaltiniais ----------
  function sukurtiPaieska(view, miestai, vietos, apskritys) {
    return new Search({
      view: view,
      includeDefaultSources: false,
      allPlaceholder: "Miestas, vieta ar adresas Lietuvoje",
      sources: [
        {
          layer: miestai,
          searchFields: ["pavadinimas"],
          displayField: "pavadinimas",
          exactMatch: false,
          outFields: ["*"],
          name: "Lietuvos miestai",
          placeholder: "pvz. Kaunas",
          zoomScale: 80000
        },
        {
          layer: vietos,
          searchFields: ["pavadinimas", "vietove"],
          suggestionTemplate: "{pavadinimas} ({vietove})",
          exactMatch: false,
          outFields: ["*"],
          name: "Lankytinos vietos",
          placeholder: "pvz. Trakų salos pilis",
          zoomScale: 20000
        },
        {
          layer: apskritys,
          searchFields: ["NAME"],
          displayField: "NAME",
          exactMatch: false,
          outFields: ["NAME", "ISO_CODE"],
          name: "Apskritys",
          placeholder: "pvz. Utenos apskritis"
        },
        {
          name: "Adresai Lietuvoje (ArcGIS World Geocoder)",
          placeholder: "pvz. Gedimino pr. 1, Vilnius",
          url: "https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer",
          countryCode: "LTU",
          singleLineFieldName: "SingleLine",
          outFields: ["Addr_type", "Match_addr"],
          zoomScale: 10000
        }
      ]
    });
  }

  return {
    atstumasKm: atstumasKm,
    TIPU_SPALVOS: TIPU_SPALVOS,
    tipoRenderer: tipoRenderer,
    sukurtiMiestus: sukurtiMiestus,
    sukurtiVietas: sukurtiVietas,
    sukurtiApskritis: sukurtiApskritis,
    sukurtiPaieska: sukurtiPaieska
  };
});
