require({
  packages: [{ name: "app", location: location.pathname.replace(/\/[^/]*$/, "") + "/js" }]
}, [
  "esri/Map",
  "esri/views/MapView",
  "esri/Graphic",
  "esri/layers/GraphicsLayer",
  "esri/geometry/Point",
  "esri/geometry/Polyline",
  "esri/geometry/operators/geodesicBufferOperator",
  "esri/rest/route",
  "esri/rest/support/RouteParameters",
  "esri/rest/support/FeatureSet",
  "esri/widgets/Home",
  "esri/widgets/Compass",
  "esri/widgets/Locate",
  "esri/widgets/ScaleBar",
  "esri/widgets/BasemapToggle",
  "esri/widgets/BasemapGallery",
  "esri/widgets/LayerList",
  "esri/widgets/Legend",
  "esri/widgets/Expand",
  "esri/widgets/Fullscreen",
  "app/common"
], function (
  Map, MapView, Graphic, GraphicsLayer, Point, Polyline, geodesicBufferOperator,
  route, RouteParameters, FeatureSet,
  Home, Compass, Locate, ScaleBar, BasemapToggle, BasemapGallery, LayerList, Legend, Expand, Fullscreen,
  common
) {
  const $ = (id) => document.getElementById(id);

  // ---------------- 1. Žemėlapio įkėlimas (2D) ----------------
  const apskritys = common.sukurtiApskritis();
  const miestai = common.sukurtiMiestus();
  const vietos = common.sukurtiVietas();
  const grafikosSluoksnis = new GraphicsLayer({ title: "Vėliava (grafika)" });
  const analizesSluoksnis = new GraphicsLayer({ title: "Analizės rezultatai", listMode: "hide" });

  const map = new Map({
    basemap: "topo-vector",
    layers: [apskritys, analizesSluoksnis, grafikosSluoksnis, vietos, miestai]
  });

  const view = new MapView({
    container: "viewDiv",
    map: map,
    center: [23.9, 55.2],
    zoom: 7,
    constraints: { minZoom: 5 },
    popup: { dockEnabled: false, dockOptions: { breakpoint: false } }
  });

  function busena(tekstas) {
    const el = $("busena");
    el.textContent = tekstas || "";
    el.style.display = tekstas ? "block" : "none";
  }

  // ---------------- 3. Valdikliai (widgets) ----------------
  view.ui.add(new Home({ view: view }), "top-left");
  view.ui.add(new Compass({ view: view }), "top-left");
  view.ui.add(new Locate({ view: view }), "top-left");
  view.ui.add(new Fullscreen({ view: view }), "top-left");
  view.ui.add(new ScaleBar({ view: view, unit: "metric" }), "bottom-left");
  view.ui.add(new BasemapToggle({ view: view, nextBasemap: "hybrid" }), "bottom-right");

  view.ui.add(new Expand({
    view: view,
    content: new LayerList({ view: view }),
    expandTooltip: "Sluoksniai",
    group: "desine"
  }), "top-right");
  view.ui.add(new Expand({
    view: view,
    content: new Legend({ view: view }),
    expandTooltip: "Legenda",
    group: "desine"
  }), "top-right");
  view.ui.add(new Expand({
    view: view,
    content: new BasemapGallery({ view: view }),
    expandTooltip: "Pagrindo žemėlapiai",
    group: "desine"
  }), "top-right");

  // ---------------- 5. Paieška su keliais šaltiniais ----------------
  const paieska = common.sukurtiPaieska(view, miestai, vietos, apskritys);
  view.ui.add(paieska, { position: "manual" });
  paieska.container.id = "paieska";
  view.container.appendChild(paieska.container);

  // ---------------- 4. Grafika: Lietuvos vėliava iš 6 elementų ----------------
  function velVieta(lon, lat) { return { type: "point", longitude: lon, latitude: lat }; }
  function staciakampis(x1, y1, x2, y2) {
    return { type: "polygon", rings: [[[x1, y1], [x2, y1], [x2, y2], [x1, y2], [x1, y1]]] };
  }

  const x0 = 25.3197, y0 = 54.9054;         // Europos centras – stiebo pagrindas
  const plotis = 1.3, juosta = 0.15, virsus = 55.65;
  const velPopup = { title: "{pavadinimas}", content: "{aprasymas}" };

  const veliava = [
    new Graphic({
      geometry: { type: "polyline", paths: [[[x0, y0], [x0, virsus + 0.05]]] },
      symbol: { type: "simple-line", color: [90, 70, 50], width: 4 },
      attributes: { pavadinimas: "Stiebas", aprasymas: "Linija (Polyline) nuo Europos centro." },
      popupTemplate: velPopup
    }),
    new Graphic({
      geometry: staciakampis(x0, virsus - juosta, x0 + plotis, virsus),
      symbol: { type: "simple-fill", color: [253, 185, 19, 0.9], outline: { color: [60, 60, 60], width: 0.5 } },
      attributes: { pavadinimas: "Geltona juosta", aprasymas: "Saulė, šviesa ir gerovė." },
      popupTemplate: velPopup
    }),
    new Graphic({
      geometry: staciakampis(x0, virsus - 2 * juosta, x0 + plotis, virsus - juosta),
      symbol: { type: "simple-fill", color: [0, 106, 68, 0.9], outline: { color: [60, 60, 60], width: 0.5 } },
      attributes: { pavadinimas: "Žalia juosta", aprasymas: "Gamta, laisvė ir viltis." },
      popupTemplate: velPopup
    }),
    new Graphic({
      geometry: staciakampis(x0, virsus - 3 * juosta, x0 + plotis, virsus - 2 * juosta),
      symbol: { type: "simple-fill", color: [193, 39, 45, 0.9], outline: { color: [60, 60, 60], width: 0.5 } },
      attributes: { pavadinimas: "Raudona juosta", aprasymas: "Drąsa ir už Lietuvą pralietas kraujas." },
      popupTemplate: velPopup
    }),
    new Graphic({
      geometry: velVieta(x0, y0),
      symbol: { type: "simple-marker", style: "circle", size: 12, color: [90, 70, 50], outline: { color: "white", width: 2 } },
      attributes: { pavadinimas: "Europos centras", aprasymas: "Taškas (Point) – stiebo pagrindas Purnuškėse." },
      popupTemplate: velPopup
    }),
    new Graphic({
      geometry: velVieta(x0 + plotis / 2, virsus + 0.1),
      symbol: {
        type: "text", text: "LIETUVA", color: [31, 41, 51],
        haloColor: "white", haloSize: 2,
        font: { size: 16, weight: "bold", family: "Noto Sans" }
      },
      attributes: { pavadinimas: "Užrašas", aprasymas: "Tekstinis simbolis (TextSymbol)." },
      popupTemplate: velPopup
    })
  ];

  $("velRodyti").onclick = function () {
    grafikosSluoksnis.removeAll();
    grafikosSluoksnis.addMany(veliava);
    view.goTo({ target: veliava, zoom: 8 });
  };
  $("velSlepti").onclick = function () { grafikosSluoksnis.removeAll(); };
  grafikosSluoksnis.addMany(veliava);

  // ---------------- 7. Vizualizacija ----------------
  const renderiai = {
    tipas: {
      aprasas: "Kiekvienas miesto tipas – atskira spalva ir forma (Visualize features by type).",
      renderer: common.tipoRenderer()
    },
    klases: {
      aprasas: "Miestai suskirstyti į 5 gyventojų skaičiaus klases (class breaks).",
      renderer: {
        type: "class-breaks",
        field: "gyventojai",
        legendOptions: { title: "Gyventojai" },
        classBreakInfos: [
          [0, 5000, "iki 5 tūkst.", "#fef0d9", 6],
          [5000, 10000, "5–10 tūkst.", "#fdcc8a", 9],
          [10000, 25000, "10–25 tūkst.", "#fc8d59", 12],
          [25000, 100000, "25–100 tūkst.", "#e34a33", 16],
          [100000, 1000000, "daugiau nei 100 tūkst.", "#b30000", 22]
        ].map(function (k) {
          return {
            minValue: k[0], maxValue: k[1], label: k[2],
            symbol: { type: "simple-marker", size: k[4], color: k[3], outline: { color: [80, 80, 80], width: 0.6 } }
          };
        })
      }
    },
    dydis: {
      aprasas: "Taško dydis tolygiai priklauso nuo gyventojų skaičiaus (continuous size).",
      renderer: {
        type: "simple",
        symbol: { type: "simple-marker", color: [0, 106, 68, 0.6], outline: { color: "white", width: 1 } },
        visualVariables: [{
          type: "size",
          field: "gyventojai",
          legendOptions: { title: "Gyventojai" },
          stops: [
            { value: 2000, size: 5, label: "2 000" },
            { value: 50000, size: 18, label: "50 000" },
            { value: 600000, size: 48, label: "600 000" }
          ]
        }]
      }
    },
    spalva: {
      aprasas: "Spalva tolygiai kinta pagal geodezinį atstumą iki Vilniaus (continuous color).",
      renderer: {
        type: "simple",
        symbol: { type: "simple-marker", size: 11, outline: { color: [60, 60, 60], width: 0.5 } },
        visualVariables: [{
          type: "color",
          field: "atstumas",
          legendOptions: { title: "Atstumas iki Vilniaus, km" },
          stops: [
            { value: 0, color: "#ffffcc", label: "0 km" },
            { value: 100, color: "#a1dab4" },
            { value: 200, color: "#41b6c4", label: "200 km" },
            { value: 320, color: "#253494", label: "320 km" }
          ]
        }]
      }
    }
  };

  function taikytiVizualizacija() {
    const r = renderiai[$("vizRezimas").value];
    miestai.renderer = r.renderer;
    $("vizAprasas").textContent = r.aprasas;
  }
  $("vizRezimas").onchange = taikytiVizualizacija;
  $("vizAprasas").textContent = renderiai.tipas.aprasas;

  // ---------------- 8. Duomenų užklausa ----------------
  let paryskinimas = null;
  let miestuLayerView = null;
  view.whenLayerView(miestai).then(function (lv) { miestuLayerView = lv; });

  function paryskinti(oids) {
    if (paryskinimas) paryskinimas.remove();
    paryskinimas = miestuLayerView && oids.length ? miestuLayerView.highlight(oids) : null;
  }

  function rodytiSarasa(el, features, antraste) {
    const sarasas = features.map(function (f, i) {
      const a = f.attributes;
      return '<li data-i="' + i + '">' + a.pavadinimas + " – " +
        a.gyventojai.toLocaleString("lt-LT") + " gyv.</li>";
    }).join("");
    el.innerHTML = antraste + (features.length ? "<ol>" + sarasas + "</ol>" : "");
    el.querySelectorAll("li").forEach(function (li) {
      li.onclick = function () {
        const f = features[+li.dataset.i];
        view.goTo({ target: f.geometry, zoom: 11 }).then(function () {
          view.openPopup({ features: [f], location: f.geometry });
        });
      };
    });
  }

  const apskrSarasas = [...new Set(window.LT_DATA.miestai.map((m) => m[1]))].sort((a, b) => a.localeCompare(b, "lt"));
  apskrSarasas.forEach(function (a) {
    const o = document.createElement("option");
    o.value = a + " apskritis";
    o.textContent = a + " apskritis";
    $("qApskritis").appendChild(o);
  });

  function sqlTekstas(s) { return "'" + s.replace(/'/g, "''") + "'"; }

  $("qVykdyti").onclick = async function () {
    const salygos = [];
    if ($("qApskritis").value) salygos.push("apskritis = " + sqlTekstas($("qApskritis").value));
    if ($("qTipas").value) salygos.push("tipas = " + sqlTekstas($("qTipas").value));
    const min = parseInt($("qMin").value, 10);
    if (!isNaN(min)) salygos.push("gyventojai >= " + min);
    const where = salygos.length ? salygos.join(" AND ") : "1=1";

    const q = miestai.createQuery();
    q.where = where;
    q.outFields = ["*"];
    q.returnGeometry = true;
    q.orderByFields = ["gyventojai DESC"];

    const stat = miestai.createQuery();
    stat.where = where;
    stat.outStatistics = [{ statisticType: "sum", onStatisticField: "gyventojai", outStatisticFieldName: "suma" }];

    const [rez, statRez] = await Promise.all([miestai.queryFeatures(q), miestai.queryFeatures(stat)]);
    const suma = statRez.features.length ? statRez.features[0].attributes.suma || 0 : 0;
    paryskinti(rez.features.map((f) => f.attributes.ObjectID));
    rodytiSarasa($("qRez"), rez.features,
      "<b>Rasta miestų: " + rez.features.length + "</b>, iš viso " +
      suma.toLocaleString("lt-LT") + " gyv.<code class='sql'>WHERE " + where + "</code>");
    if (rez.features.length) view.goTo(rez.features);
  };

  $("qValyti").onclick = function () {
    paryskinti([]);
    $("qRez").innerHTML = "";
  };

  // ---------------- 6. Analizė: geodezinis buferis ----------------
  let rezimas = null;

  function nustatytiRezima(r) {
    rezimas = r;
    view.popupEnabled = !r;
    $("buferisBtn").classList.toggle("aktyvus", r === "buferis");
    busena(r === "buferis" ? "Spustelėkite žemėlapyje buferio centrą" : "");
  }

  $("buferisBtn").onclick = function () { nustatytiRezima(rezimas === "buferis" ? null : "buferis"); };
  $("buferisValyti").onclick = function () {
    analizesSluoksnis.graphics.removeMany(analizesSluoksnis.graphics.filter((g) => g.attributes && g.attributes.buferis));
    $("buferisRez").innerHTML = "";
    paryskinti([]);
  };

  view.on("click", async function (e) {
    if (rezimas !== "buferis") return;
    e.stopPropagation();
    nustatytiRezima(null);
    const km = Math.max(1, parseFloat($("buferisKm").value) || 30);

    if (!geodesicBufferOperator.isLoaded()) await geodesicBufferOperator.load();
    const buferis = geodesicBufferOperator.execute(e.mapPoint, km, { unit: "kilometers" });

    $("buferisValyti").onclick();
    analizesSluoksnis.addMany([
      new Graphic({
        geometry: buferis,
        symbol: { type: "simple-fill", color: [193, 39, 45, 0.15], outline: { color: [193, 39, 45], width: 2 } },
        attributes: { buferis: true }
      }),
      new Graphic({
        geometry: e.mapPoint,
        symbol: { type: "simple-marker", style: "cross", size: 14, outline: { color: [193, 39, 45], width: 3 } },
        attributes: { buferis: true }
      })
    ]);

    const q = miestai.createQuery();
    q.geometry = buferis;
    q.spatialRelationship = "intersects";
    q.outFields = ["*"];
    q.returnGeometry = true;
    q.orderByFields = ["gyventojai DESC"];
    const rez = await miestai.queryFeatures(q);
    paryskinti(rez.features.map((f) => f.attributes.ObjectID));
    const suma = rez.features.reduce((s, f) => s + f.attributes.gyventojai, 0);
    rodytiSarasa($("buferisRez"), rez.features,
      "<b>" + km + " km</b> spinduliu: " + rez.features.length + " miest., " +
      suma.toLocaleString("lt-LT") + " gyv.");
    view.goTo(buferis.extent.expand(1.2));
  });

  // ---------------- 6. Analizė: maršrutas ----------------
  const miestuPavad = window.LT_DATA.miestai.map((m) => m[0]).sort((a, b) => a.localeCompare(b, "lt"));
  ["mIs", "mPer", "mI"].forEach(function (id) {
    const sel = $(id);
    if (id === "mPer") sel.innerHTML = '<option value="">—</option>';
    miestuPavad.forEach(function (p) {
      const o = document.createElement("option");
      o.value = o.textContent = p;
      sel.appendChild(o);
    });
  });
  $("mIs").value = "Vilnius";
  $("mPer").value = "Trakai";
  $("mI").value = "Kaunas";

  function miestoTaskas(pav) {
    const m = window.LT_DATA.miestai.find((x) => x[0] === pav);
    return new Point({ longitude: m[4], latitude: m[5] });
  }

  async function esriMarsrutas(taskai, raktas) {
    const params = new RouteParameters({
      apiKey: raktas,
      stops: new FeatureSet({
        features: taskai.map((p, i) => new Graphic({ geometry: p, attributes: { Name: "Stotelė " + (i + 1) } }))
      }),
      outSpatialReference: view.spatialReference,
      returnDirections: false
    });
    const rez = await route.solve(
      "https://route-api.arcgis.com/arcgis/rest/services/World/Route/NAServer/Route_World", params);
    const r = rez.routeResults[0].route;
    return { geometry: r.geometry, km: r.attributes.Total_Kilometers, min: r.attributes.Total_TravelTime, saltinis: "Esri Route" };
  }

  async function osrmMarsrutas(taskai) {
    const koord = taskai.map((p) => p.longitude.toFixed(5) + "," + p.latitude.toFixed(5)).join(";");
    const atsakas = await fetch("https://router.project-osrm.org/route/v1/driving/" + koord +
      "?overview=full&geometries=geojson");
    const duom = await atsakas.json();
    if (duom.code !== "Ok") throw new Error(duom.message || duom.code);
    const r = duom.routes[0];
    return {
      geometry: new Polyline({ paths: [r.geometry.coordinates], spatialReference: { wkid: 4326 } }),
      km: r.distance / 1000,
      min: r.duration / 60,
      saltinis: "OSRM (OpenStreetMap)"
    };
  }

  $("marsrutasBtn").onclick = async function () {
    const pav = [$("mIs").value, $("mPer").value, $("mI").value].filter(Boolean);
    const taskai = pav.map(miestoTaskas);
    $("marsrutasValyti").onclick();
    $("marsrutasRez").textContent = "Skaičiuojama…";
    try {
      const raktas = (window.APP_CONFIG || {}).arcgisApiKey;
      const r = raktas ? await esriMarsrutas(taskai, raktas) : await osrmMarsrutas(taskai);
      analizesSluoksnis.add(new Graphic({
        geometry: r.geometry,
        symbol: { type: "simple-line", color: [0, 106, 68, 0.85], width: 5 },
        attributes: { marsrutas: true }
      }));
      taskai.forEach(function (p, i) {
        analizesSluoksnis.add(new Graphic({
          geometry: p,
          symbol: {
            type: "simple-marker", size: 18, color: i === 0 ? [0, 106, 68] : i === taskai.length - 1 ? [193, 39, 45] : [253, 185, 19],
            outline: { color: "white", width: 2 }
          },
          attributes: { marsrutas: true }
        }));
      });
      const val = Math.floor(r.min / 60), min = Math.round(r.min % 60);
      $("marsrutasRez").innerHTML = "<b>" + pav.join(" → ") + "</b><br>Atstumas: " +
        r.km.toFixed(1).replace(".", ",") + " km · Laikas: " + (val ? val + " val. " : "") + min + " min." +
        "<br><span class='pastaba'>Šaltinis: " + r.saltinis + "</span>";
      view.goTo(r.geometry.extent.expand(1.3));
    } catch (err) {
      $("marsrutasRez").textContent = "Nepavyko apskaičiuoti maršruto: " + err.message;
    }
  };

  $("marsrutasValyti").onclick = function () {
    analizesSluoksnis.graphics.removeMany(analizesSluoksnis.graphics.filter((g) => g.attributes && g.attributes.marsrutas));
    $("marsrutasRez").innerHTML = "";
  };

  window.gisView = view;
  window.gisPaieska = paieska;
});
