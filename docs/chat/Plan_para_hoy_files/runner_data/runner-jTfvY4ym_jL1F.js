try{window.__oaiDilBootstrap?.moduleEvaluationStarted?.()}catch{}(function(){const e=document.createElement("link").relList;if(e&&e.supports&&e.supports("modulepreload"))return;for(const o of document.querySelectorAll('link[rel="modulepreload"]'))n(o);new MutationObserver(o=>{for(const i of o)if(i.type==="childList")for(const s of i.addedNodes)s.tagName==="LINK"&&s.rel==="modulepreload"&&n(s)}).observe(document,{childList:!0,subtree:!0});function t(o){const i={};return o.integrity&&(i.integrity=o.integrity),o.referrerPolicy&&(i.referrerPolicy=o.referrerPolicy),o.crossOrigin==="use-credentials"?i.credentials="include":o.crossOrigin==="anonymous"?i.credentials="omit":i.credentials="same-origin",i}function n(o){if(o.ep)return;o.ep=!0;const i=t(o);fetch(o.href,i)}})();const b=14,D=4e3,z=500,K=2,J="__dil_confirm_startup";function Q(r){return r!=null&&typeof r=="object"&&r.__oaiDilFrame===!0&&r.kind==="createRunner"&&r.protocolVersion===b&&typeof r.compiledDil=="string"&&typeof r.runnerId=="string"}function X(r){return r!=null&&typeof r=="object"&&r.__oaiDilFrame===!0&&r.kind==="disposeRunner"&&r.protocolVersion===b&&typeof r.runnerId=="string"}var k={exports:{}},G;function Y(){return G||(G=1,(function(r,e){r.path="dil_renderer/src/DILDictation",Object.defineProperty(e,"__esModule",{value:!0}),e.normalizeDILDictationOptions=e.DILDictationEventKind=e.DILDictationStatus=void 0;var t;(function(i){i.Idle="idle",i.Starting="starting",i.Recording="recording",i.Stopping="stopping",i.Done="done",i.Error="error"})(t||(e.DILDictationStatus=t={}));var n;(function(i){i.Recording="recording",i.Transcript="transcript",i.Stopping="stopping",i.Done="done",i.Error="error",i.Cancelled="cancelled"})(n||(e.DILDictationEventKind=n={}));function o(i){if(i===null||typeof i!="object"||Array.isArray(i))return null;const s=i.language;return s===void 0?{}:typeof s!="string"||!s.trim()||s.length>128?null:{language:s}}e.normalizeDILDictationOptions=o})(k,k.exports)),k.exports}Y();globalThis.crypto?.getRandomValues.bind(globalThis.crypto);globalThis.crypto?.getRandomValues.bind(globalThis.crypto);function Z(r,e){return r==="speechState"&&e.speechSynthesis===!0||r==="dictationEvent"&&e.dictation===!0}const ee=["host_setup","encode_globals","read_persistence","acquire_frame","iframe_create","iframe_load","iframe_resource","observer_bootstrap","module_load","module_resource","module_entry","frame_ready","manager_create","worker_create","worker_control","worker_module","harden_worker","runner_create","worker_ready","decode_globals","decode_persistence","unsafe_runner","runtime_construct","seed_runtime","hydrate_state","prepare_source","update_source","compile","evaluate","render","render_operations","snapshot_post","commit_state","persist_state","flush","flush_render","flush_effects","restore_source","command","host_call","snapshot_receive","snapshot_forward","apply_tree","notify","react_snapshot","react_materialize","react_commit","react_snapshot_effect","react_node_error","host_error","fallback","render_result","frame_timer","worker_timer","request_timer","health_probe","idle_probe","visibility","pagehide","pageshow","freeze","resume","frame_probe","event_loop","recovery","quarantine","runner_dispose","worker_stop","frame_dispose","message","error","coverage"],A=typeof performance>"u"?Date.now.bind(Date):performance.now.bind(performance),te=typeof performance>"u"?0:performance.timeOrigin,M=Object.getOwnPropertyDescriptor,ne=Object.getPrototypeOf,oe=Error.isError,re=new Map([[Error.prototype,"Error"],[TypeError.prototype,"TypeError"],[RangeError.prototype,"RangeError"],[SyntaxError.prototype,"SyntaxError"],[ReferenceError.prototype,"ReferenceError"],[URIError.prototype,"URIError"],[EvalError.prototype,"EvalError"],[AggregateError.prototype,"AggregateError"]]);let ie=0;const R=256,se=32768,N=512,E=32,v=64,ae=65536,le=8192,de=12288,j=40,q=new Set(["iframe_create:returned","iframe_load:received","iframe_resource:received","observer_bootstrap:returned","module_entry:returned","module_load:begin","module_resource:returned","module_load:reported_error","runner_module_script_error","runner_module_csp_enforced","runner_module_exception","frame_ready:sent","frame_ready:received","worker_create:begin","worker_create:returned","worker_control:sent","worker_control:received","worker_module:returned","harden_worker:begin","harden_worker:returned","runner_create:sent","runner_create:received","runner_create:begin","runner_create:returned","worker_ready:sent","worker_ready:received","snapshot_post:sent","snapshot_receive:received","host_ingress_lifecycle","host_ingress_snapshot","snapshot_forward:sent","apply_tree:returned","request_timer:fired","worker_timer:fired","frame_timer:fired"]);function W(r){if(r.operation==="module_load"&&r.reason==="runner_module_exception")return"runner_module_exception";if(r.fault||r.phase==="threw")return"first_fault";if(r.operation==="module_load"&&r.phase==="reported_error"){if(r.reason==="runner_module_script_error")return"runner_module_script_error";if(r.reason==="csp_violation"&&r.cspResource==="runner_module"&&r.cspDisposition==="enforce")return"runner_module_csp_enforced"}if(r.operation==="quarantine"||r.operation==="error")return r.operation;if(r.operation==="worker_control"&&(r.reason==="execution_started"||r.reason==="execution_finished"))return"worker_execution";if(r.realm==="host"&&r.operation==="message"&&r.phase==="received"&&(r.command==="snapshot"||r.command==="lifecycle"))return`host_ingress_${r.command}`;const e=`${r.operation}:${r.phase}`;if(q.has(e))return e;if(r.operation==="health_probe"&&r.reason?.startsWith("target_"))return"worker_target";if(r.operation==="health_probe")return r.phase==="fired"?"health_probe_expired":"health_probe";if(r.operation==="recovery")return"recovery";if(r.operation==="event_loop")return"event_loop";if(["visibility","pagehide","pageshow","freeze","resume"].includes(r.operation))return"lifecycle";if(r.operation==="frame_probe")return"frame_probe";if(r.operation==="module_load"&&r.reason==="document_state")return"document_state";if(r.operation==="module_resource")return"module_resource";if(r.operation!=="message"&&r.operation!=="coverage")return"last_progress"}function T(r){return[r.realm,r.documentBootId,r.realm==="worker"?r.workerGeneration:void 0,r.sourceId].join(":")}const ce=new Set(ee),ue=new Set(["begin","returned","threw","reported_error","sent","received","acknowledged","resolved","rejected","armed","paused","rearmed","cancelled","fired","ignored","decision"]),pe=new Set(["initialize","__dil_initialize","createRunner","disposeRunner","initializeControl","registerMessengerTransport","unregisterMessengerTransport","setCompiledDil","setData","setStateSnapshot","trigger","invokeGlobal","invokeMessengerCommand","snapshot","lifecycle","messengerEvent"]),_e=["workerGeneration","sessionGeneration","startupAttempt","snapshotVersion","renderPass","spanId","parentSpanId","count","pendingRequests","pendingHostCalls","peers","budgetMs","deadlineMs","elapsedMs","httpStatus","resourceStartMs","resourceEndMs","navigationResponseEndMs","navigationDomInteractiveMs"],me=["triggerAcknowledged","workerReadySent","hidden","persisted","acknowledged","replaySafe","hasSuccessfulSnapshot","treePresent","hasError","moduleEvaluationSupported"],he=["documentBootId","artifactId","runnerId","activeRunnerId","resourceArtifactId","triggerRunnerId","triggerRequestId","requestId","relatedRequestId"],H=/^[a-zA-Z0-9_-]{1,96}$/;function O(r){const e={};for(const t of _e){const n=r[t];typeof n=="number"&&Number.isFinite(n)&&n>=(t==="httpStatus"?100:0)&&(t!=="httpStatus"||n<=599)&&(e[t]=n)}for(const t of me)typeof r[t]=="boolean"&&(e[t]=r[t]);for(const t of he){const n=r[t];typeof n=="string"&&H.test(n)&&(e[t]=n)}for(const t of["command","triggerCommand"]){const n=r[t];n&&pe.has(n)&&(e[t]=n)}return(r.documentReadyState==="loading"||r.documentReadyState==="interactive"||r.documentReadyState==="complete")&&(e.documentReadyState=r.documentReadyState),r.cspDirective&&["script-src","script-src-elem","script-src-attr","worker-src","other"].includes(r.cspDirective)&&(e.cspDirective=r.cspDirective),(r.cspDisposition==="enforce"||r.cspDisposition==="report")&&(e.cspDisposition=r.cspDisposition),r.cspResource&&["runner_module","inline","eval","unknown"].includes(r.cspResource)&&(e.cspResource=r.cspResource),typeof r.reason=="string"&&/^[a-z_]{1,48}$/.test(r.reason)&&(e.reason=r.reason),e}function ge(r){const e=[];let t="unknown";try{if(oe?.(r)){t=re.get(ne(r))??t;const n=M(r,"name")?.value;typeof n=="string"&&/^(Error|TypeError|RangeError|SyntaxError|ReferenceError|URIError|EvalError|AggregateError)$/.test(n)&&(t=n);const o=M(r,"stack")?.value;if(typeof o=="string"){t=/^(Error|TypeError|RangeError|SyntaxError|ReferenceError|URIError|EvalError|AggregateError)(?=:|$)/.exec(o)?.[1]??t;for(const i of o.slice(0,4096).split(`
`).slice(1,17)){const s=/:(\d{1,7}):(\d{1,7})\)?$/.exec(i.trim());s&&e.push({line:Number(s[1]),column:Number(s[2])})}}}}catch{}return Object.freeze({name:t,locations:Object.freeze(e.map(n=>Object.freeze(n))),stackUnavailable:e.length===0})}function C(r){if(r===null||typeof r!="object")return;const e=r;if(!["host","bootstrap","iframe","worker"].includes(e.realm)||typeof e.operation!="string"||!ce.has(e.operation)||typeof e.phase!="string"||!ue.has(e.phase)||!Number.isSafeInteger(e.sequence)||e.sequence<0||typeof e.localTimeMs!="number"||!Number.isFinite(e.localTimeMs)||typeof e.timeOriginMs!="number"||!Number.isFinite(e.timeOriginMs))return;const t={...O(e),realm:e.realm,sequence:e.sequence,localTimeMs:e.localTimeMs,timeOriginMs:e.timeOriginMs,operation:e.operation,phase:e.phase};Number.isSafeInteger(e.sourceId)&&e.sourceId>=0&&(t.sourceId=e.sourceId);const n=e.fault;if(n&&typeof n.name=="string"&&/^(unknown|Error|TypeError|RangeError|SyntaxError|ReferenceError|URIError|EvalError|AggregateError)$/.test(n.name)&&Array.isArray(n.locations)&&n.locations.length<=16){const o=n.locations.filter(i=>i&&Number.isSafeInteger(i.line)&&i.line>=0&&i.line<1e7&&Number.isSafeInteger(i.column)&&i.column>=0&&i.column<1e7).map(({line:i,column:s})=>Object.freeze({line:i,column:s}));t.fault=Object.freeze({name:n.name,locations:Object.freeze(o),stackUnavailable:o.length===0})}return t}function y(r){return[r.realm,r.documentBootId,r.workerGeneration,r.sourceId,r.runnerId,r.sessionGeneration,r.startupAttempt].join(":")}function P(r,e,t){const n=t?e.hostReceivedAtMs:A();return{...r,...typeof n=="number"&&Number.isFinite(n)&&n>=0?{hostReceivedAtMs:n}:{}}}class fe{context;now=A;realm;emit;sequence=0;sourceId=++ie;span=0;parentSpanId;dropped=0;bytes=0;faultCount=0;emitted=0;open=new Map;completedSpans=new Map;observedRealms=new Set;upstreamDropped=new Map;upstreamTruncated=!1;entries=[];sequences=new Map;progress=new Map;progressSources=new Map;retainedRunners=new Map;progressBytes=0;progressDropped=0;progressLoss=new Map;constructor(e){this.realm=e.realm,this.context=Object.freeze(O(e.context??{})),this.emit=e.emit}record(e,t,n={}){try{const o={...this.context,...O(n),realm:this.realm,sequence:++this.sequence,sourceId:this.sourceId,localTimeMs:A(),timeOriginMs:te,operation:e,phase:t};n.error!==void 0&&this.faultCount<4&&(o.fault=ge(n.error),this.faultCount++),this.retain(o,"complete"),this.forward(o),this.sequence===N&&this.record("coverage","ignored",{reason:"overflow"})}catch{this.dropped++}}trace(e,t,n={}){const o=++this.span,i=this.parentSpanId,s={...n,spanId:o,parentSpanId:i};this.record(e,"begin",s),this.parentSpanId=o;try{const l=t();return this.record(e,"returned",s),l}catch(l){throw this.fault(e,l,s),l}finally{this.parentSpanId=i}}fault(e,t,n={}){this.record(e,"threw",{...n,error:t})}ingest(e,t=!1,n=!1){try{const o=C(e);if(!o){this.dropped++;return}const i=y(o);if(o.sequence<=(this.sequences.get(i)??-1))return;this.sequences.set(i,o.sequence),this.sequences.size>32&&this.sequences.delete(this.sequences.keys().next().value);const s=P(o,e,n),l=T(o),a=this.progressSources.get(l),d=a?a.complete&&o.sequence===a.sequence+1&&o.operation!=="coverage":o.sequence===1&&o.operation!=="coverage";if(o.sequence>(a?.sequence??-1)&&(this.progressSources.set(l,{sequence:o.sequence,complete:d}),this.progressSources.size>v&&this.progressSources.delete(this.progressSources.keys().next().value),!d))for(const m of this.progress.values())T(m.slots.values().next().value)===l&&(m.coverage="interrupted");this.retain(s,d?"complete":"interrupted"),t&&this.forward(o)}catch{this.dropped++}}mergeSnapshot(e,t=!1,n,o="merge"){try{if(e===null||typeof e!="object")return this.dropped++,!1;const i=e;if(i.schemaVersion!==1||!Array.isArray(i.records)||!Array.isArray(i.openOperations))return this.dropped++,!1;if(n){const c=h=>h!==null&&typeof h=="object"&&Object.entries(n).every(([x,w])=>w===void 0||h[x]===w),_=[...i.records.slice(0,R),...i.openOperations.slice(0,E)];if(Array.isArray(i.progress))for(const h of i.progress.slice(0,v)){if(!h||!Array.isArray(h.records))return this.markProgressDropped(n.runnerId),!1;for(const x of h.records.slice(0,j))_.push({...h.context,...x})}if(!_.every(c))return this.markProgressDropped(n.runnerId),!1}if(Array.isArray(i.observedRealms))for(const c of i.observedRealms.slice(0,4))(c==="host"||c==="bootstrap"||c==="iframe"||c==="worker")&&this.observedRealms.add(c);for(const c of i.records.slice(0,R))this.ingest(c,!1,t);o==="replace"&&Array.isArray(i.progress)&&this.resetDocumentProgress();for(const c of i.openOperations.slice(0,E)){const _=C(c);if(!_||_.phase!=="begin"||_.spanId===void 0){this.dropped++;continue}this.observeSpan(P(_,c,t))}let s=!0;if(Array.isArray(i.progress)){const c=this.progressDropped;for(const _ of i.progress.slice(0,v))this.mergeProgress(_,t);i.progress.length>v&&this.markProgressDropped(),s=c===this.progressDropped}if(Number.isSafeInteger(i.progressDropped)&&i.progressDropped>0){this.progressDropped=Math.max(this.progressDropped,i.progressDropped);const c=typeof i.runnerId=="string"&&H.test(i.runnerId)&&(this.progressLoss.has(i.runnerId)||this.progressLoss.size<v)?i.runnerId:"";this.progressLoss.set(c,Math.max(this.progressLoss.get(c)??0,i.progressDropped))}const l=C(i.records[0]??i.openOperations[0]),a=l?y(l):"empty",d=this.upstreamDropped.get(a)??0,m=typeof i.dropped=="number"&&Number.isSafeInteger(i.dropped)&&i.dropped>=0?i.dropped:0,f=Math.max(0,i.records.length-R)+Math.max(0,i.openOperations.length-E),I=Math.min(Number.MAX_SAFE_INTEGER,m+f);return this.dropped=Math.min(Number.MAX_SAFE_INTEGER,this.dropped+Math.max(0,I-d)),this.upstreamDropped.set(a,Math.max(d,I)),this.upstreamDropped.size>32&&this.upstreamDropped.delete(this.upstreamDropped.keys().next().value),this.upstreamTruncated||=i.truncated===!0||f>0,s}catch{return this.dropped++,!1}}resetDocumentProgress(){this.progress.clear(),this.progressSources.clear(),this.progressLoss.clear(),this.progressBytes=0,this.progressDropped=0,this.open.clear(),this.completedSpans.clear()}retainRunner(e){if(!this.retainedRunners.has(e)&&this.retainedRunners.size>=v)return this.markProgressDropped(e),()=>{};this.retainedRunners.set(e,(this.retainedRunners.get(e)??0)+1);let t=!1;return()=>{if(t)return;t=!0;const n=(this.retainedRunners.get(e)??1)-1;n?this.retainedRunners.set(e,n):this.retainedRunners.delete(e)}}snapshot(e={}){const t=[...this.progress.values()].filter(l=>{const a=l.slots.values().next().value;return!e.runnerId||!a.runnerId||a.runnerId===e.runnerId}).map(l=>{const a=[...l.slots.values()],{realm:d,sourceId:m,timeOriginMs:f,documentBootId:I,artifactId:c,runnerId:_,workerGeneration:h,sessionGeneration:x,startupAttempt:w}=a[0];return{context:{realm:d,sourceId:m,timeOriginMs:f,documentBootId:I,artifactId:c,runnerId:_,workerGeneration:h,sessionGeneration:x,startupAttempt:w},records:a.map(({realm:De,sourceId:je,timeOriginMs:Pe,documentBootId:Ue,artifactId:Le,runnerId:Ae,workerGeneration:Oe,sessionGeneration:ze,startupAttempt:Ge,...$})=>$),lastSequence:l.lastSequence,coverage:l.coverage}}),n=e.runnerId?[...ve(t,e.runnerId,this.context.documentBootId)].sort((l,a)=>+(a.context.runnerId===e.runnerId)-+(l.context.runnerId===e.runnerId)):t,o=[];let i=2,s=0;for(const l of n){const a=JSON.stringify(JSON.stringify(l)).length+1;if(i+a>le){s++;continue}i+=a,o.push(Object.freeze({...l,context:Object.freeze(l.context),records:Object.freeze(l.records.map(d=>Object.freeze(d)))}))}return Object.freeze({schemaVersion:1,observedRealms:Object.freeze([...this.observedRealms]),documentBootId:this.context.documentBootId,runnerId:e.runnerId,progress:Object.freeze(o),progressDropped:s+(e.runnerId?(this.progressLoss.get(e.runnerId)??0)+(this.progressLoss.get("")??0):this.progressDropped),records:Object.freeze(e.records===!1?[]:this.entries.map(l=>l.record)),openOperations:Object.freeze(e.records===!1?[]:[...this.open.values()]),dropped:this.dropped,truncated:this.dropped>0||this.upstreamTruncated})}markProgressDropped(e){this.progressDropped++;const t=e&&(this.progressLoss.has(e)||this.progressLoss.size<v)?e:"";this.progressLoss.set(t,(this.progressLoss.get(t)??0)+1)}observeProgress(e,t,n){const o=W(e);if(!o)return;const i=y(e);let s=this.progress.get(i);const l=s?.slots.get(o);if(s&&(s.lastSequence=Math.max(s.lastSequence,e.sequence)),l&&o==="event_loop"&&(l.elapsedMs??0)-(l.budgetMs??0)>=(e.elapsedMs??0)-(e.budgetMs??0)||l&&(o==="first_fault"||q.has(o)?l.sequence<=e.sequence:l.sequence>=e.sequence))return;n??=JSON.stringify(e).length;const a=n-(l?JSON.stringify(l).length:0);for(;!s&&this.progress.size>=v||this.progressBytes+a>ae;){const d=[...this.progress].find(([m,f])=>{const I=f.slots.values().next().value.runnerId;return m!==i&&I&&!this.retainedRunners.has(I)});if(!d){this.markProgressDropped(e.runnerId),s&&(s.coverage="interrupted");return}this.progressBytes-=d[1].bytes,this.progress.delete(d[0]),this.markProgressDropped(d[1].slots.values().next().value.runnerId)}if(s||(s={slots:new Map,lastSequence:0,coverage:t,bytes:0},this.progress.set(i,s)),s.bytes+a>de||!l&&s.slots.size>=j){this.markProgressDropped(e.runnerId),s.coverage="interrupted";return}s.slots.set(o,Object.freeze(e)),s.lastSequence=Math.max(s.lastSequence,e.sequence),t!=="complete"&&(s.coverage=t),s.bytes+=a,this.progressBytes+=a}mergeProgress(e,t){if(!e||typeof e!="object"){this.markProgressDropped();return}const n=e;if(!Array.isArray(n.records)||n.records.length===0||n.records.length>j||!Number.isSafeInteger(n.lastSequence)||n.lastSequence<0||!["complete","interrupted","unavailable"].includes(n.coverage??"")){this.markProgressDropped();return}const o=n.records.map(d=>C({...n.context,...d})),i=o[0];if(!i||o.some(d=>!d||y(d)!==y(i)||d.sequence>n.lastSequence||!W(d))){this.markProgressDropped();return}const s=this.progress.get(y(i));if(s&&n.lastSequence<s.lastSequence)return;const l=this.progressDropped;for(let d=0;d<o.length;d++)this.observeProgress(P(o[d],n.records[d],t),n.coverage);const a=this.progress.get(y(i));a&&(a.lastSequence=Math.max(a.lastSequence,n.lastSequence),a.coverage=this.progressDropped===l?n.coverage:"interrupted")}observeSpan(e){if(e.spanId!==void 0){const t=`${y(e)}:${e.spanId}`;if(e.phase==="begin"){if((this.completedSpans.get(t)??-1)>=e.sequence||(this.open.get(t)?.sequence??-1)>=e.sequence)return;this.open.has(t)||this.open.size<E?this.open.set(t,Object.freeze(e)):this.dropped++}else(e.phase==="returned"||e.phase==="threw")&&(this.open.delete(t),this.completedSpans.set(t,e.sequence),this.completedSpans.size>R&&this.completedSpans.delete(this.completedSpans.keys().next().value))}}retain(e,t){const n=JSON.stringify(e).length;this.observeProgress(e,t,n),this.observedRealms.add(e.realm),this.observeSpan(e);const o=this.entries.length<8||e.fault!==void 0&&!this.entries.some(i=>i.record.fault);for(;this.entries.length>=R||this.bytes+n>se;){const i=this.entries.findIndex(s=>!s.pinned);if(i<0){this.dropped++;return}this.bytes-=this.entries[i].bytes,this.entries.splice(i,1),this.dropped++}this.entries.push({record:Object.freeze(e),bytes:n,pinned:o}),this.bytes+=n}forward(e){if(this.emit){if(this.emitted++>=N+1){this.dropped++;return}try{this.emit(e)}catch{this.dropped++}}}}function ve(r,e,t){const n=r.filter(({context:a})=>(!e||!a.runnerId||a.runnerId===e)&&(!t||!a.documentBootId||a.documentBootId===t)),o=n.map(a=>a.context).filter(a=>a.runnerId===e),i=Math.max(-1,...o.map(a=>a.workerGeneration??-1)),s=Math.max(-1,...o.filter(a=>a.workerGeneration===i).map(a=>a.sessionGeneration??-1)),l=Math.max(-1,...o.filter(a=>a.workerGeneration===i&&a.sessionGeneration===s).map(a=>a.startupAttempt??-1));return n.filter(({context:a})=>(a.workerGeneration===void 0||a.workerGeneration===i)&&(a.sessionGeneration===void 0||a.sessionGeneration===s)&&(a.startupAttempt===void 0||a.startupAttempt===l))}const u="__dil_initialize",U="__dil_startup_confirmation_",ye=1e4,be=16;class Ie{activeRunnerId=null;executionEpoch=0;disposed=!1;createChannel;createWorker;diagnostics;healthChecks=new Map;diagnosticProbeCount=0;diagnosticProbes=new Map;healthCheckTimeoutMs;idleHealthCheck=null;isVisible;nextMessageId=0;nextSessionGeneration=0;requestTimeoutMs;sessions=new Map;retiredDiagnosticSessions=new Map;unattributedRestarts=0;visibilityDocument;worker=null;workerControlPort=null;workerGeneration=0;constructor(e){this.diagnostics=e.diagnostics,this.createChannel=e.createChannel??(()=>new MessageChannel),this.createWorker=e.createWorker,this.healthCheckTimeoutMs=e.healthCheckTimeoutMs??z,this.requestTimeoutMs=e.requestTimeoutMs??2e3,this.isVisible=e.isVisible??(()=>typeof document>"u"||!document.hidden),this.visibilityDocument=typeof document>"u"?null:document,this.visibilityDocument?.addEventListener("visibilitychange",this.handleVisibilityChange),this.diagnostics?.record("manager_create","begin",this.diagnosticDetails()),this.startWorker(),this.diagnostics?.record("manager_create","returned",this.diagnosticDetails())}createRunner(e,t){if(this.disposed){t.close();return}this.disposeRunner(e.runnerId);const n={diagnosticFailureReported:!1,diagnosticGeneration:++this.nextSessionGeneration,releaseDiagnosticRunner:this.diagnostics?.retainRunner(e.runnerId),hasSuccessfulSnapshot:!1,handleHostMessage:o=>{this.receiveHostMessage(n,o.data)},handleWorkerMessage:o=>{this.receiveWorkerMessage(n,o.data,o.currentTarget)},hostPort:t,lastSnapshotVersion:0,message:{...e,...e.statePersistence?{statePersistence:{...e.statePersistence}}:{}},pendingRequests:new Map,pendingWorkerCommands:new Map,quarantined:!1,restoringCommands:new Set,startupAttempts:0,startupRetrySafe:!0,transports:new Set,workerPort:null};if(this.sessions.set(e.runnerId,n),this.diagnostics?.record("runner_create","received",this.diagnosticDetails(n)),t.addEventListener("message",n.handleHostMessage),t.start(),!this.worker&&!this.startWorker()){this.failSession(n,"worker_start_error");return}this.startSession(n)}disposeRunner(e){const t=this.sessions.get(e);if(!t)return;const n=t.pendingRequests.has(u)&&this.isOnlyActiveSession(t);if(this.diagnostics?.record("runner_dispose","begin",this.diagnosticDetails(t)),this.sessions.delete(e),!this.disposed&&this.diagnostics&&t.pendingRequests.has(u)){this.releaseRetiredDiagnosticSession(e),this.retiredDiagnosticSessions.size>=be&&this.releaseRetiredDiagnosticSession(this.retiredDiagnosticSessions.keys().next().value);const o=globalThis.setTimeout(()=>this.releaseRetiredDiagnosticSession(e),ye);this.retiredDiagnosticSessions.set(e,{workerGeneration:this.workerGeneration,sessionGeneration:t.diagnosticGeneration,startupAttempt:t.startupAttempts,timeout:o,releaseDiagnosticRunner:t.releaseDiagnosticRunner})}else t.releaseDiagnosticRunner?.();this.cancelHealthChecks(t),this.clearRequests(t,!1),this.closeWorkerPort(t),t.hostPort.removeEventListener("message",t.handleHostMessage),t.hostPort.close(),this.notifyWorkerToDispose(e),this.activeRunnerId===e&&(this.activeRunnerId=null),n&&this.checkIdleWorker(),this.diagnostics?.record("runner_dispose","returned",this.diagnosticDetails(t))}dispose(){if(!this.disposed){this.disposed=!0;for(const e of this.retiredDiagnosticSessions.keys())this.releaseRetiredDiagnosticSession(e);this.visibilityDocument?.removeEventListener("visibilitychange",this.handleVisibilityChange);for(const e of[...this.sessions.keys()])this.disposeRunner(e);this.stopWorker()}}probeTarget(e,t){if(!this.diagnostics||!e||this.disposed){t();return}const n=this.sessions.get(e),o=n?void 0:this.retiredDiagnosticSessions.get(e),i={...this.diagnosticContext(n),...o?{workerGeneration:o.workerGeneration,sessionGeneration:o.sessionGeneration,startupAttempt:o.startupAttempt}:{},runnerId:e},s=this.workerControlPort;if(!s||this.diagnosticProbeCount++>=4){this.diagnostics.record("health_probe","ignored",{...i,reason:s?"target_probe_budget_exhausted":"target_control_unavailable"}),t();return}const l=`diagnostic_${this.nextMessageId++}`,a=(m,f)=>{if(this.diagnosticProbes.delete(l)){globalThis.clearTimeout(d),this.diagnostics?.record("health_probe","received",{...i,requestId:l,reason:`target_${m}`,workerReadySent:f});try{t()}catch{}}},d=globalThis.setTimeout(()=>a("no_response"),z);this.diagnosticProbes.set(l,{context:i,finish:a}),this.diagnostics.record("health_probe","sent",{...i,requestId:l,reason:"target_requested"});try{s.postMessage({__oaiDilWorker:!0,kind:"healthCheck",requestId:l,runnerId:e})}catch{a("control_unavailable")}}releaseRetiredDiagnosticSession(e){const t=this.retiredDiagnosticSessions.get(e);t&&(globalThis.clearTimeout(t.timeout),t.releaseDiagnosticRunner?.(),this.retiredDiagnosticSessions.delete(e))}startWorker(){if(this.disposed)return!1;let e,t,n="worker_create";this.workerGeneration+=1,this.diagnostics?.record(n,"begin",this.diagnosticDetails());try{return e=this.createWorker(),this.diagnostics?.record(n,"returned",this.diagnosticDetails()),n="worker_control",this.diagnostics?.record(n,"begin",this.diagnosticDetails()),t=this.createChannel(),t.port1.addEventListener("message",this.receiveWorkerControl),t.port1.start(),e.postMessage({__oaiDilWorker:!0,kind:"initializeControl",protocolVersion:b,...this.diagnostics?{diagnostics:this.diagnosticContext()}:{}},[t.port2]),this.diagnostics?.record(n,"sent",this.diagnosticDetails()),this.worker=e,this.workerControlPort=t.port1,!0}catch(o){return this.diagnostics?.fault(n,o,this.diagnosticDetails()),t?.port1.removeEventListener("message",this.receiveWorkerControl),t?.port1.close(),t?.port2.close(),e?.terminate(),this.worker=null,this.workerControlPort=null,!1}}stopWorker(){this.diagnostics?.record("worker_stop","begin",{...this.diagnosticDetails(),count:this.healthChecks.size}),this.cancelIdleHealthCheck();for(const n of this.diagnosticProbes.values())n.finish(this.disposed?"disposed":"worker_replaced");for(const n of this.healthChecks.values())globalThis.clearTimeout(n.timeout);this.healthChecks.clear(),this.activeRunnerId=null;const e=this.workerControlPort;this.workerControlPort=null,e?.removeEventListener("message",this.receiveWorkerControl),e?.close();const t=this.worker;this.worker=null,t?.terminate(),this.diagnostics?.record("worker_stop","returned",this.diagnosticDetails())}checkIdleWorker(){const e=this.workerControlPort;if(!e||this.disposed)return;this.cancelIdleHealthCheck();const t={controlPort:e,requestId:`idle_health_${this.nextMessageId++}`,timeout:null};this.idleHealthCheck=t,this.armIdleHealthCheck(t);try{e.postMessage({__oaiDilWorker:!0,kind:"healthCheck",requestId:t.requestId}),this.diagnostics?.record("idle_probe","sent",{...this.diagnosticDetails(),requestId:t.requestId})}catch(n){this.diagnostics?.fault("idle_probe",n,{...this.diagnosticDetails(),requestId:t.requestId}),this.idleHealthCheck===t&&this.stopWorker()}}armIdleHealthCheck(e){if(e.timeout!=null&&globalThis.clearTimeout(e.timeout),e.timeout=null,this.visibilityDocument&&!this.isVisible()){this.diagnostics?.record("idle_probe","paused",{...this.diagnosticDetails(),requestId:e.requestId,hidden:!0});return}const t=globalThis.setTimeout(()=>{if(!(this.idleHealthCheck!==e||this.workerControlPort!==e.controlPort||e.timeout!==t)){if(!this.isVisible()){this.armIdleHealthCheck(e);return}this.diagnostics?.record("idle_probe","fired",{...this.diagnosticDetails(),requestId:e.requestId}),this.stopWorker()}},D+this.healthCheckTimeoutMs);e.timeout=t,this.diagnostics?.record("idle_probe","armed",{...this.diagnosticDetails(),requestId:e.requestId,budgetMs:D+this.healthCheckTimeoutMs})}cancelIdleHealthCheck(){const e=this.idleHealthCheck;e&&this.diagnostics?.record("idle_probe","cancelled",{...this.diagnosticDetails(),requestId:e.requestId}),this.idleHealthCheck=null,e?.timeout!=null&&globalThis.clearTimeout(e.timeout)}startSession(e){if(e.quarantined||this.disposed)return;if(this.idleHealthCheck&&(this.stopWorker(),!this.startWorker())){this.failSession(e,"worker_start_error");return}const t=this.workerControlPort;if(!t){this.diagnostics?.record("runner_create","ignored",{...this.diagnosticDetails(e),reason:"control_unavailable"});return}this.diagnostics?.record("runner_create","begin",{...this.diagnosticDetails(e),startupAttempt:e.startupAttempts+1}),this.closeWorkerPort(e);const n=this.createChannel();e.startupAttempts+=1,e.workerPort=n.port1,n.port1.addEventListener("message",e.handleWorkerMessage),n.port1.start(),this.trackRequest(e,u,"initialize");try{t.postMessage(this.diagnostics?{...e.message,diagnosticContext:this.diagnosticContext(e)}:e.message,[n.port2]),this.diagnostics?.record("runner_create","sent",this.diagnosticDetails(e));for(const o of e.transports){const i=`__dil_restore_${this.nextMessageId++}`;e.restoringCommands.add(i),n.port1.postMessage({__oaiDilMessage:!0,command:"registerMessengerTransport",data:{channelId:o},id:i,kind:"command"}),this.diagnostics?.record("command","sent",{...this.diagnosticDetails(e),requestId:i,command:"registerMessengerTransport",reason:"restore_transport"})}}catch(o){this.diagnostics?.fault("runner_create",o,this.diagnosticDetails(e)),this.failSession(e,"worker_start_error")}}receiveHostMessage(e,t){if(!this.isLiveSession(e)||!V(t))return;if(t.kind==="response"){if(typeof t.id!="string")return;const o=e.pendingWorkerCommands.get(t.id);if(!o){this.diagnostics?.record("host_call","ignored",{...this.diagnosticDetails(e),requestId:t.id,reason:"stale_generation"});return}this.diagnostics?.record("host_call","received",{...this.diagnosticDetails(e),requestId:t.id,relatedRequestId:o}),e.pendingWorkerCommands.delete(t.id),e.workerPort?.postMessage({...t,id:o}),this.diagnostics?.record("host_call",e.workerPort?"sent":"ignored",{...this.diagnosticDetails(e),requestId:o});return}if(t.kind==="event"){!e.quarantined&&Z(t.event,e.message)&&e.workerPort?.postMessage(t);return}if(t.kind!=="command"||typeof t.id!="string")return;this.diagnostics?.record("command","received",{...this.diagnosticDetails(e),requestId:t.id,command:t.command});const n=this.rememberHostCommand(e,t);if(e.quarantined){if(t.command==="setCompiledDil"&&n){if(this.diagnostics?.record("recovery","decision",{...this.diagnosticDetails(e),reason:"source_changed"}),e.diagnosticGeneration=++this.nextSessionGeneration,e.diagnosticFailureReported=!1,e.hasSuccessfulSnapshot=!1,e.startupAttempts=0,e.startupRetrySafe=!0,e.quarantined=!1,this.unattributedRestarts=0,this.replyToHost(e,t.id,t.command),!this.worker&&!this.startWorker()){this.failSession(e,"worker_start_error");return}this.startSession(e)}else this.replyToHost(e,t.id,t.command);return}if(!e.workerPort){this.replyToHost(e,t.id,t.command);return}this.trackRequest(e,t.id,t.command??"unknown"),e.workerPort.postMessage({...t,...t.command==="trigger"?{acknowledge:!0}:{}}),this.diagnostics?.record("command","sent",{...this.diagnosticDetails(e),requestId:t.id,command:t.command})}rememberHostCommand(e,t){if(!p(t.data))return e.startupRetrySafe=!1,!1;switch(t.command){case"setCompiledDil":{if(typeof t.data.compiledDil!="string")return!1;const n=e.message.compiledDil!==t.data.compiledDil||t.data.dilComponents!==void 0;return e.message.compiledDil=t.data.compiledDil,t.data.dilComponents!==void 0&&(e.message.dilComponents=t.data.dilComponents),t.data.data!==void 0&&(e.message.data=t.data.data),n}case"setData":return e.message.data=t.data.data,!1;case"setStateSnapshot":if(e.message.statePersistence){const n=t.data.widgetId;if(typeof n=="string"){const o={...e.message.statePersistence.widgetSnapshots};if(t.data.snapshot===void 0)delete o[n];else{const i=L(t.data.snapshot);i&&Object.defineProperty(o,n,{value:i,enumerable:!0,configurable:!0,writable:!0})}return e.message.statePersistence.widgetSnapshots=o,!1}if(t.data.snapshot===void 0)delete e.message.statePersistence.snapshot;else{const o=L(t.data.snapshot);o&&(e.message.statePersistence.snapshot=o)}}return!1;case"registerMessengerTransport":return typeof t.data.channelId=="string"&&e.transports.add(t.data.channelId),!1;case"unregisterMessengerTransport":return typeof t.data.channelId=="string"&&e.transports.delete(t.data.channelId),!1;default:return e.startupRetrySafe=!1,!1}}receiveWorkerMessage(e,t,n){if(!this.isLiveSession(e)||!V(t))return;if(t.kind==="response"&&typeof t.id=="string"&&t.id.startsWith(U)){const i=t.id.slice(U.length),s=this.healthChecks.get(i);s?.session===e&&s.confirmationPort===n&&n===e.workerPort&&this.finishStartupConfirmation(i);return}let o=t;if(this.diagnostics?.record(t.kind==="event"&&t.event==="lifecycle"&&p(t.data)&&t.data.kind==="worker_ready"?"worker_ready":"message","received",{...this.diagnosticDetails(e),requestId:t.id,command:t.command}),(t.kind!=="event"||t.event!=="lifecycle"||!p(t.data)||t.data.kind!=="worker_ready")&&(e.startupRetrySafe=!1),t.kind==="ack"){const i=typeof t.id=="string"?e.pendingRequests.get(t.id):void 0;i?.command==="trigger"&&(i.acknowledged=!0,this.diagnostics?.record("command","acknowledged",{...this.diagnosticDetails(e),requestId:t.id,command:i.command,acknowledged:!0}));return}if(t.kind==="response"){if(typeof t.id!="string"||e.restoringCommands.delete(t.id))return;this.diagnostics?.record("command",t.error?"rejected":"resolved",{...this.diagnosticDetails(e),requestId:t.id}),this.clearRequest(e,t.id)}if(t.kind==="event"&&t.event==="snapshot"){if(this.diagnostics?.record("snapshot_receive","received",{...this.diagnosticDetails(e),...p(t.data)&&typeof t.data.version=="number"?{snapshotVersion:t.data.version}:{}}),p(t.data)&&t.data.error!=null){this.diagnostics?.record("snapshot_receive","reported_error",{...this.diagnosticDetails(e),error:t.data.error});const s=this.captureFailure(e);s&&(o={...t,data:{...t.data,diagnostics:s}})}const i=e.pendingRequests.has(u);if(this.clearRequest(e,u),i)for(const[s,l]of[...e.pendingRequests])this.trackRequest(e,s,l.command);p(t.data)&&typeof t.data.version=="number"&&(t.data.error==null&&(e.hasSuccessfulSnapshot=!0,e.diagnosticFailureReported=!1),e.lastSnapshotVersion=Math.max(e.lastSnapshotVersion,t.data.version))}if(t.kind==="command"&&typeof t.id=="string"){this.rememberPersistedState(e,t);const i=`worker_${this.nextMessageId}`;this.nextMessageId+=1,e.pendingWorkerCommands.set(i,t.id),this.diagnostics?.record("host_call","sent",{...this.diagnosticDetails(e),requestId:i,relatedRequestId:t.id,command:t.command}),e.hostPort.postMessage({...t,id:i});return}e.hostPort.postMessage(o),t.kind==="event"&&t.event==="lifecycle"&&p(t.data)&&t.data.kind==="worker_ready"&&this.diagnostics?.record("worker_ready","sent",this.diagnosticDetails(e)),t.kind==="event"&&t.event==="snapshot"&&this.diagnostics?.record("snapshot_forward","sent",{...this.diagnosticDetails(e),snapshotVersion:e.lastSnapshotVersion})}rememberPersistedState(e,t){const n=e.message.statePersistence;if(!n||t.command!=="invokeGlobal"||!p(t.data)||t.data.id!==n.persistState.__oaiDilGlobalFunction||!Array.isArray(t.data.args))return;const o=L(t.data.args[0]);if(o){const i=t.data.args[1];typeof i=="string"?n.widgetSnapshots={...n.widgetSnapshots,[i]:o}:i===void 0&&(n.snapshot=o)}}handleVisibilityChange=()=>{if(this.disposed)return;this.idleHealthCheck&&this.armIdleHealthCheck(this.idleHealthCheck);const e=this.isVisible();this.diagnostics?.record("visibility","received",{...this.diagnosticDetails(),hidden:!e});for(const t of this.sessions.values()){this.cancelHealthChecks(t);for(const[n,o]of[...t.pendingRequests])e?this.trackRequest(t,n,o.command):o.timeout!=null&&(globalThis.clearTimeout(o.timeout),o.timeout=null,delete o.diagnosticDeadlineMs,this.diagnostics?.record("request_timer","paused",{...this.diagnosticDetails(t),requestId:n,command:o.command,hidden:!0}))}};trackRequest(e,t,n){const o=e.pendingRequests.get(t),i=o?.acknowledged??!1;this.clearRequest(e,t);const s=n==="initialize"||e.pendingRequests.has(u)?D:this.requestTimeoutMs,l=this.diagnostics?.now(),a=this.visibilityDocument&&!this.isVisible()?null:globalThis.setTimeout(()=>{this.requestTimedOut(e,t)},s);e.pendingRequests.set(t,{acknowledged:i,command:n,timeout:a,...l!=null?{diagnosticStartedAt:l,...a!=null?{diagnosticDeadlineMs:l+s}:{}}:{}}),this.diagnostics?.record("request_timer",a==null?"paused":o?"rearmed":"armed",{...this.diagnosticDetails(e),requestId:t,command:n,budgetMs:s,deadlineMs:a!=null&&l!=null?l+s:void 0,acknowledged:i,hidden:a==null})}clearRequest(e,t){const n=e.pendingRequests.get(t);n&&(this.diagnostics?.record("request_timer","cancelled",{...this.diagnosticDetails(e),requestId:t,command:n.command,acknowledged:n.acknowledged,deadlineMs:n.diagnosticDeadlineMs}),n.timeout!=null&&globalThis.clearTimeout(n.timeout),e.pendingRequests.delete(t),this.cancelHealthChecks(e,t))}clearRequests(e,t){this.diagnostics?.record("request_timer","cancelled",{...this.diagnosticDetails(e),reason:"clear_all"});for(const[n,o]of e.pendingRequests)o.timeout!=null&&globalThis.clearTimeout(o.timeout),t&&n!==u&&this.replyToHost(e,n,o.command);e.pendingRequests.clear(),e.pendingWorkerCommands.clear(),e.restoringCommands.clear()}requestTimedOut(e,t){if(!this.isLiveSession(e)||e.quarantined||!e.pendingRequests.has(t)||e.pendingRequests.has(u)&&(t=u,[...this.healthChecks.values()].some(a=>a.session===e&&a.requestId===t)))return;const n=e.pendingRequests.get(t);if(this.diagnostics?.record("request_timer","fired",{...this.diagnosticDetails(e),requestId:t,command:n?.command,acknowledged:n?.acknowledged,deadlineMs:n?.diagnosticDeadlineMs,elapsedMs:n?.diagnosticStartedAt!=null?this.diagnostics.now()-n.diagnosticStartedAt:void 0}),!this.isVisible()){const a=e.pendingRequests.get(t)?.command;a&&this.trackRequest(e,t,a);return}const o=this.diagnosticTrigger(e,t),i=this.workerControlPort;if(!i){if(this.diagnostics?.record("health_probe","ignored",{...this.diagnosticDetails(e),requestId:t,reason:"control_unavailable"}),this.recoverStartup(e,o))return;this.canRestoreCallback(e,t)||this.quarantineSession(e,"control_unavailable",o),this.restartWorker(o);return}const s=`health_${this.nextMessageId}`;this.nextMessageId+=1;const l=globalThis.setTimeout(()=>{this.healthCheckTimedOut(s)},this.healthCheckTimeoutMs);this.healthChecks.set(s,{executionEpoch:this.executionEpoch,diagnosticStartedAt:this.diagnostics?.now(),requestId:t,session:e,timeout:l}),this.diagnostics?.record("health_probe","armed",{...this.diagnosticDetails(e),requestId:s,relatedRequestId:t,budgetMs:this.healthCheckTimeoutMs,deadlineMs:this.diagnostics.now()+this.healthCheckTimeoutMs,acknowledged:n?.acknowledged});try{i.postMessage({__oaiDilWorker:!0,kind:"healthCheck",requestId:s,...this.diagnostics?{runnerId:e.message.runnerId}:{}}),this.diagnostics?.record("health_probe","sent",{...this.diagnosticDetails(e),requestId:s})}catch(a){this.diagnostics?.fault("health_probe",a,{...this.diagnosticDetails(e),requestId:s}),this.healthCheckTimedOut(s)}}receiveWorkerControl=e=>{if(xe(e.data))switch(e.data.kind){case"diagnostic":{if(!this.diagnostics||e.currentTarget!==this.workerControlPort)return;const t=C(e.data.record);if(!t||t.realm!=="worker"||t.documentBootId!==this.diagnostics.context.documentBootId||t.workerGeneration!==this.workerGeneration)return;if(t.runnerId){const n=this.sessions.get(t.runnerId),o=this.retiredDiagnosticSessions.get(t.runnerId);if(!(n&&t.sessionGeneration===n.diagnosticGeneration&&t.startupAttempt===n.startupAttempts||!n&&o&&t.workerGeneration===o.workerGeneration&&t.sessionGeneration===o.sessionGeneration&&t.startupAttempt===o.startupAttempt))return}this.diagnostics.ingest(t,!0);break}case"executionStarted":this.diagnostics?.record("worker_control","received",{...this.diagnosticDetails(this.sessions.get(e.data.runnerId)),reason:"execution_started"}),this.executionEpoch+=1,this.activeRunnerId=e.data.runnerId;break;case"executionFinished":this.diagnostics?.record("worker_control","received",{...this.diagnosticDetails(this.sessions.get(e.data.runnerId)),reason:"execution_finished"}),this.activeRunnerId===e.data.runnerId&&(this.activeRunnerId=null);break;case"healthy":{const t=this.diagnosticProbes.get(e.data.requestId);if(t){if(e.currentTarget!==this.workerControlPort)return;const s=e.data.target,l=s&&Object.entries(t.context).every(([a,d])=>!s.present&&(a==="sessionGeneration"||a==="startupAttempt")||s[a]===d);e.data.diagnostics&&(!s||l)&&this.diagnostics?.mergeSnapshot(e.data.diagnostics,!1,t.context),t.finish(s?l?s.present===!0?"present":s.present===!1?"absent":"status_unavailable":"changed":"status_unavailable",l&&typeof s?.workerReadySent=="boolean"?s.workerReadySent:void 0);return}const n=this.idleHealthCheck;if(n?.requestId===e.data.requestId){this.diagnostics?.record("idle_probe","received",{...this.diagnosticDetails(),requestId:e.data.requestId}),n.controlPort===this.workerControlPort&&this.cancelIdleHealthCheck();return}const o=this.healthChecks.get(e.data.requestId);if(!o||o.confirmationPort)return;this.diagnostics&&e.currentTarget===this.workerControlPort&&e.data.diagnostics&&this.diagnostics.mergeSnapshot(e.data.diagnostics,!1,this.diagnosticContext(o.session)),this.diagnostics?.record("health_probe","received",{...this.diagnosticDetails(o.session),requestId:e.data.requestId,relatedRequestId:o.requestId,budgetMs:this.healthCheckTimeoutMs,elapsedMs:o.diagnosticStartedAt===void 0?void 0:this.diagnostics.now()-o.diagnosticStartedAt,acknowledged:o.session.pendingRequests.get(o.requestId)?.acknowledged});const i=o.session.pendingRequests.get(o.requestId);if(this.isLiveSession(o.session)&&i&&this.isVisible()&&o.requestId===u&&!this.isOnlyActiveSession(o.session)&&o.session.workerPort){o.confirmationPort=o.session.workerPort;try{o.confirmationPort.postMessage({__oaiDilMessage:!0,command:J,id:U+e.data.requestId,kind:"command"})}catch{this.finishStartupConfirmation(e.data.requestId)}return}if(globalThis.clearTimeout(o.timeout),this.healthChecks.delete(e.data.requestId),this.isLiveSession(o.session)&&i){if(!this.isVisible()){this.requestTimedOut(o.session,o.requestId);return}const s=this.diagnosticTrigger(o.session,o.requestId);if(this.recoverStartup(o.session,s))return;i.acknowledged?this.trackRequest(o.session,o.requestId,i.command):this.canRestoreCallback(o.session,o.requestId)?this.restartSession(o.session,s):this.quarantineSession(o.session,"probe_healthy_unacknowledged",s)}break}}};finishStartupConfirmation(e){const t=this.healthChecks.get(e);if(t?.confirmationPort&&(globalThis.clearTimeout(t.timeout),this.healthChecks.delete(e),!(!this.isLiveSession(t.session)||t.confirmationPort!==t.session.workerPort||!t.session.pendingRequests.has(u)))){if(!this.isVisible()){this.requestTimedOut(t.session,u);return}this.quarantineSession(t.session,"probe_healthy_unacknowledged",this.diagnosticTrigger(t.session,u))}}healthCheckTimedOut(e){const t=this.healthChecks.get(e);if(!t)return;if(this.diagnostics?.record("health_probe","fired",{...this.diagnosticDetails(t.session),requestId:e,relatedRequestId:t.requestId,budgetMs:this.healthCheckTimeoutMs,elapsedMs:t.diagnosticStartedAt===void 0?void 0:this.diagnostics.now()-t.diagnosticStartedAt,acknowledged:t.session.pendingRequests.get(t.requestId)?.acknowledged,count:this.unattributedRestarts}),t.confirmationPort){this.finishStartupConfirmation(e);return}if(this.healthChecks.delete(e),globalThis.clearTimeout(t.timeout),!this.isLiveSession(t.session))return;if(!this.isVisible()){this.requestTimedOut(t.session,t.requestId);return}const n=t.executionEpoch===this.executionEpoch&&this.activeRunnerId?this.sessions.get(this.activeRunnerId):void 0,o=this.diagnosticTrigger(t.session,t.requestId);this.recoverStartup(t.session,o)||(n?(this.unattributedRestarts=0,this.canRestoreCallback(n,n===t.session?t.requestId:void 0)||this.quarantineSession(n,"peer_probe_timeout",o)):this.canRestoreCallback(t.session,t.requestId)?this.unattributedRestarts=0:this.unattributedRestarts>0?this.quarantineSession(t.session,"probe_timeout",o):this.unattributedRestarts+=1,this.restartWorker(o))}recoverStartup(e,t){return e.hasSuccessfulSnapshot||!e.pendingRequests.has(u)||!this.isOnlyActiveSession(e)?!1:(this.canRetryStartup(e)?(this.diagnostics?.record("recovery","decision",{...this.diagnosticDetails(e),reason:"startup_retry",...t}),this.restartWorker(t)):(this.diagnostics?.record("recovery","decision",{...this.diagnosticDetails(e),reason:"startup_exhausted",...t}),this.stopWorker(),this.quarantineSession(e,"startup_exhausted",t)),!0)}canRetryStartup(e){return!e.quarantined&&(e.message.recoverSafeStartupPeers===!0||this.isOnlyActiveSession(e))&&e.startupRetrySafe&&e.startupAttempts<K}isOnlyActiveSession(e){return!e.quarantined&&[...this.sessions.values()].every(t=>t===e||t.quarantined)}canRestoreCallback(e,t){return e.hasSuccessfulSnapshot?t!=null?e.pendingRequests.get(t)?.command==="trigger":[...e.pendingRequests.values()].some(n=>n.command==="trigger"):!1}restartSession(e,t){this.diagnostics?.record("recovery","decision",{...this.diagnosticDetails(e),reason:"session_restart",...t}),this.cancelHealthChecks(e),this.clearRequests(e,!0),this.closeWorkerPort(e),this.notifyWorkerToDispose(e.message.runnerId),this.startSession(e)}restartWorker(e){this.diagnostics?.record("recovery","decision",{...this.diagnosticDetails(),reason:"worker_restart",...e,count:this.unattributedRestarts});const t=[...this.sessions.values()].filter(o=>!o.hasSuccessfulSnapshot&&!this.canRetryStartup(o));for(const o of t)this.quarantineSession(o,"worker_restart",e);this.stopWorker();const n=[...this.sessions.values()].filter(o=>!o.quarantined);if(n.length!==0){for(const o of n)this.clearRequests(o,!0),this.closeWorkerPort(o);if(!this.startWorker()){for(const o of n)this.failSession(o,"worker_start_error");return}for(const o of n)this.startSession(o)}}quarantineSession(e,t,n){if(!this.isLiveSession(e)||e.quarantined)return;this.diagnostics?.record("quarantine","decision",{...this.diagnosticDetails(e),reason:t,...n});const o=this.captureFailure(e);e.quarantined=!0,this.cancelHealthChecks(e),this.clearRequests(e,!0),this.closeWorkerPort(e),this.notifyWorkerToDispose(e.message.runnerId),this.failSession(e,"worker_timeout",o)}failSession(e,t,n){this.diagnostics?.record("error","reported_error",{...this.diagnosticDetails(e),reason:t});const o=n??this.captureFailure(e);e.quarantined=!0,this.clearRequests(e,!0),this.closeWorkerPort(e),e.lastSnapshotVersion+=1,e.hostPort.postMessage({__oaiDilMessage:!0,data:{...o?{diagnostics:o}:{},error:{message:t==="worker_timeout"?"The DIL runner request timed out":"The DIL runner worker could not be started",name:"Error"},tree:null,version:e.lastSnapshotVersion},event:"snapshot",kind:"event"}),e.hostPort.postMessage({__oaiDilMessage:!0,data:{kind:"failure",stage:t},event:"lifecycle",kind:"event"})}cancelHealthChecks(e,t){for(const[n,o]of this.healthChecks)o.session===e&&(t==null||o.requestId===t)&&(this.diagnostics?.record("health_probe","cancelled",{...this.diagnosticDetails(e),requestId:n,relatedRequestId:o.requestId}),globalThis.clearTimeout(o.timeout),this.healthChecks.delete(n))}closeWorkerPort(e){e.workerPort&&(this.diagnostics?.record("message","cancelled",this.diagnosticDetails(e)),e.workerPort.removeEventListener("message",e.handleWorkerMessage),e.workerPort.close(),e.workerPort=null,e.pendingWorkerCommands.clear(),e.restoringCommands.clear())}notifyWorkerToDispose(e){try{this.workerControlPort?.postMessage({__oaiDilFrame:!0,kind:"disposeRunner",protocolVersion:b,runnerId:e}),this.diagnostics?.record("runner_dispose",this.workerControlPort?"sent":"ignored",{...this.diagnosticDetails(this.sessions.get(e)),runnerId:e})}catch(t){this.diagnostics?.fault("runner_dispose",t,{...this.diagnosticDetails(this.sessions.get(e)),runnerId:e})}}replyToHost(e,t,n){e.hostPort.postMessage({__oaiDilMessage:!0,id:t,kind:"response",...n==="setCompiledDil"||n==="setData"?{result:{inputApplied:!1}}:{}}),this.diagnostics?.record("command","resolved",{...this.diagnosticDetails(e),requestId:t})}diagnosticTrigger(e,t){const n=e.pendingRequests.get(t);return{triggerRunnerId:e.message.runnerId,triggerRequestId:t,triggerCommand:n?.command,triggerAcknowledged:n?.acknowledged}}diagnosticContext(e){return{...this.diagnostics?.context,workerGeneration:this.workerGeneration,...e?{runnerId:e.message.runnerId,sessionGeneration:e.diagnosticGeneration,startupAttempt:e.startupAttempts}:{}}}diagnosticDetails(e){return{...this.diagnosticContext(e),activeRunnerId:this.activeRunnerId??void 0,peers:this.sessions.size,...e?{pendingRequests:e.pendingRequests.size,pendingHostCalls:e.pendingWorkerCommands.size,replaySafe:e.startupRetrySafe,hasSuccessfulSnapshot:e.hasSuccessfulSnapshot,snapshotVersion:e.lastSnapshotVersion}:{}}}captureFailure(e){if(!(!this.diagnostics||e.diagnosticFailureReported))return e.diagnosticFailureReported=!0,this.diagnostics.snapshot({runnerId:e.message.runnerId})}isLiveSession(e){return!this.disposed&&this.sessions.get(e.message.runnerId)===e}}function V(r){return p(r)&&r.__oaiDilMessage===!0&&(r.kind==="ack"||r.kind==="command"||r.kind==="event"||r.kind==="response")}function xe(r){return p(r)&&r.__oaiDilWorker===!0&&(r.kind==="diagnostic"?p(r.record):r.kind==="healthCheck"||r.kind==="healthy"?typeof r.requestId=="string":(r.kind==="executionStarted"||r.kind==="executionFinished")&&typeof r.runnerId=="string")}function p(r){return typeof r=="object"&&r!==null}function L(r){if(!(!p(r)||Array.isArray(r)))try{return JSON.parse(JSON.stringify(r))}catch{return}}const B=`(function(){"use strict";var oe=typeof globalThis<"u"?globalThis:typeof window<"u"?window:typeof global<"u"?global:typeof self<"u"?self:{},_t={exports:{}},re;function ot(){return re||(re=1,(function(a,t){a.path="dil_renderer/src/DILApiFacade",Object.defineProperty(t,"__esModule",{value:!0}),t.freezeObjectGraph=t.readDILModelNamedEntity=t.isDILModelEntityReference=t.createDILModelGenUIFacade=t.createDILGenUIFacade=t.hardenApiFacade=t.bindDILClientAction=void 0;const n=new WeakMap;function r(D,i){const p=Object.assign({},D);return n.set(p,i),p}t.bindDILClientAction=r;const l=3,o=128,u=/^turn(?:0|[1-9]\\d*)(product|business)(?:0|[1-9]\\d*)(?![\\s\\S])/,d="GenUI.openEntityDetail requires one reference or named entity.",s=new Set(["product","business","local_business","restaurant","hotel","hospital","point_of_interest","urgent_care","entity_sidebar_follow_up"]);function m(D){return Object.setPrototypeOf(D,null),P(D,!1),D}t.hardenApiFacade=m;function h(D){return m({dispatchAction(i,p){const y=g(i,0,n.get(i));D(y,y.handler==="client"&&y.type==="log_event"?p:void 0)},dispatchWidgetAction(i){const p=i.nativeWidgetResolutionId,y=i.componentName;if(typeof p!="string"||!p||typeof y!="string"||!y)throw new Error("Registered component action is missing its bound target.");D(Object.assign(Object.assign({},g(i,0,void 0)),{handler:"server",nativeWidgetResolutionId:p,componentName:y}))},logEvent(i){D({handler:"client",payload:i,type:"log_event"})},issueNewTurn(i,p){D({handler:"client",payload:Object.assign(Object.assign({},p),{query:i}),type:"issue_new_turn"})},openEntityDetail(i,p,y){const b={category:i,query:p};y!==void 0&&(b.extraParams=y),D({handler:"client",payload:b,type:"open_entity_detail"})},openImageLightbox(i,p){D({handler:"client",payload:{images:i,selected_index:p},type:"open_image_lightbox"})},openUrl(i){D({handler:"client",payload:{url:i},type:"open_url"})}})}t.createDILGenUIFacade=h;function x(D){return m({copy(i){if(arguments.length!==1||typeof i!="string")throw new TypeError("GenUI.copy requires one text string.");D.dispatchAction({handler:"client",payload:{value:i},type:"copy"})},issueNewTurn(i){if(arguments.length!==1||typeof i!="string")throw new TypeError("GenUI.issueNewTurn requires one query string.");D.issueNewTurn(i)},openEntityDetail(i){if(arguments.length!==1)throw new TypeError(d);if(i!==null&&typeof i=="object"&&"refId"in i){const p=e(i);D.openEntityDetail(p.category,p.refId)}else{const p=c(i);D.dispatchAction({handler:"client",type:"open_entity_detail",payload:Object.assign({},p)})}},openUrl(i){if(arguments.length!==1||typeof i!="string")throw new TypeError("GenUI.openUrl requires one URL string.");D.openUrl(i)}})}t.createDILModelGenUIFacade=x;function _(D,i){var p;return i.length<=o&&((p=u.exec(i))===null||p===void 0?void 0:p[1])===D}t.isDILModelEntityReference=_;function e(D){if(D===null||typeof D!="object"||Array.isArray(D))throw new TypeError(d);const i=D,p=Reflect.ownKeys(i),y=i.category,b=i.refId;if(p.length!==2||p.some(E=>E!=="category"&&E!=="refId")||y!=="product"&&y!=="business"||typeof b!="string"||!_(y,b))throw new TypeError(d);return{category:y,refId:b}}function c(D){if(D===null||typeof D!="object"||Array.isArray(D))throw new TypeError(d);const i=D,p=Reflect.ownKeys(i);if(!p.includes("category")||!p.includes("value")||p.some(b=>b!=="category"&&b!=="value"&&b!=="disambig"))throw new TypeError(d);const y=f(i.category,128).toLowerCase();if(s.has(y))throw new TypeError(d);return Object.assign({category:y,value:f(i.value,1024)},p.includes("disambig")?{disambig:f(i.disambig,1024)}:{})}t.readDILModelNamedEntity=c;function f(D,i){if(typeof D!="string"||D.length>i||!D.trim())throw new TypeError(d);return D.trim()}function g(D,i,p){var y;if(D===null||typeof D!="object"||Array.isArray(D))throw new Error("GenUI.dispatchAction requires an action object.");const b=D,E={handler:(y=w(b.handler,"handler"))!==null&&y!==void 0?y:"client"},k=w(b.type,"type");k!==void 0&&(E.type=k);const L=w(b.loadingBehavior,"loadingBehavior");L!==void 0&&(E.loadingBehavior=L);const j=b.payload;if(j!==void 0){if(j===null||typeof j!="object"||Array.isArray(j))throw new Error("GenUI.dispatchAction requires payload to be an object.");E.payload=Object.assign({},j),delete E.payload.followup_metadata}const z=b.fallbackAction;if(z!==void 0){if(i>=l)throw new Error("GenUI.dispatchAction exceeds the maximum fallback depth.");E.fallbackAction=g(z,i+1,p)}return p===void 0?E:Object.assign(Object.assign({},E),p)}function w(D,i){if(D!==void 0){if(typeof D!="string")throw new Error(\`GenUI.dispatchAction requires \${i} to be a string.\`);return D}}function P(D,i){O(D,i,new WeakSet)}t.freezeObjectGraph=P;function O(D,i,p){if(!(!V(D)||p.has(D))){p.add(D),i&&O(Object.getPrototypeOf(D),i,p);for(const y of Reflect.ownKeys(D)){const b=Object.getOwnPropertyDescriptor(D,y);b!==void 0&&("value"in b?O(b.value,i,p):(O(b.get,i,p),O(b.set,i,p)))}if(Object.freeze(D),!Object.isFrozen(D))throw new Error("Failed to freeze an object in the object graph.")}}function V(D){return typeof D=="object"&&D!==null||typeof D=="function"}})(_t,_t.exports)),_t.exports}var mt=ot(),ft={exports:{}},ie;function st(){return ie||(ie=1,(function(a,t){a.path="dil_renderer/src/DILElement",Object.defineProperty(t,"__esModule",{value:!0}),t.createDilElement=t.isDILRuntimeElement=t.readRuntimeElementKey=t.propsWithChildren=t.createDilJsxRuntime=t.getDilComponentResolver=t.getDilComponentScope=t.createDilComponentPlaceholder=t.getDilElementInteractionScope=t.setDilElementInteractionScope=t.DIL_RENDERED_PROP_NAME=t.DIL_RENDERED_PROP_ELEMENT_TYPE=t.TEXT_ELEMENT_TYPE=t.DIL_FRAGMENT=void 0;const n=Symbol("openai.valdi.dil.runtimeElement");t.DIL_FRAGMENT=Symbol("oai.dil.fragment"),t.TEXT_ELEMENT_TYPE="text",t.DIL_RENDERED_PROP_ELEMENT_TYPE="__dil-rendered-prop",t.DIL_RENDERED_PROP_NAME="__dilRenderedPropName";const r=new WeakMap,l=new WeakMap,o=new WeakMap;function u(g,w){o.set(g,{scope:w})}t.setDilElementInteractionScope=u;function d(g){return o.get(g)}t.getDilElementInteractionScope=d;function s(g,w,P){const O=()=>{throw new Error(\`DIL component placeholder '\${g}' must be used as a JSX element.\`)};return Object.defineProperty(O,"name",{value:g}),r.set(O,w),P!==void 0&&l.set(O,P),O}t.createDilComponentPlaceholder=s;function m(g){return l.get(g)}t.getDilComponentScope=m;function h(g){return r.get(g)}t.getDilComponentResolver=h;function x(g){return{Fragment:t.DIL_FRAGMENT,jsx:(w,P,...O)=>{const V=f(w,P,O);return g?.(V),V}}}t.createDilJsxRuntime=x;function _(g,w){const P={};if(g)for(const O in g)O!=="key"&&(P[O]=g[O]);return w.length===1?P.children=w[0]:w.length>1&&(P.children=w),P}t.propsWithChildren=_;function e(g){var w;const P=(w=g.props)===null||w===void 0?void 0:w.key;if(typeof P=="string"||typeof P=="number")return P}t.readRuntimeElementKey=e;function c(g){return g!==null&&typeof g=="object"&&g[n]===!0}t.isDILRuntimeElement=c;function f(g,w,P){return{[n]:!0,type:g,props:w??void 0,children:P}}t.createDilElement=f})(ft,ft.exports)),ft.exports}var ht=st(),gt={exports:{}},se;function Ht(){return se||(se=1,(function(a,t){a.path="dil_renderer/src/DILHtmlViewMessenger",Object.defineProperty(t,"__esModule",{value:!0}),t.getDILHtmlViewMessengerHost=t.createDILHtmlViewMessengerApiFacade=t.createDILHtmlViewMessengerHost=t.DILHtmlViewMessengerApiImpl=void 0;const n=ot(),r=Symbol("DILHtmlViewMessengerHost");class l{constructor(h,x){this.renderer=h,this.eventHandlers=new Map,this.options=x,this.channelId=h.nextHtmlViewMessengerChannelId(),this.host=o(this.channelId,{command:(_,e)=>this.receiveCommand(_,e),event:(_,e)=>this.receiveEvent(_,e),registerTransport:_=>this.renderer.registerHtmlViewMessengerTransport(this.channelId,_)}),this.unregisterReceiver=h.registerHtmlViewMessengerReceiver(this.channelId,{command:(_,e)=>this.receiveCommand(_,e),event:(_,e)=>this.receiveEvent(_,e)})}updateOptions(h){this.options=h}receiveCommand(h,x){var _,e;const c=(e=(_=this.options)===null||_===void 0?void 0:_.commands)===null||e===void 0?void 0:e[h];if(c===void 0)throw new Error(\`Unknown DIL html-view messenger command "\${h}"\`);return c(x)}receiveEvent(h,x){const _=this.eventHandlers.get(h);if(_!==void 0)for(const e of _)e(x)}emit(h,x){this.renderer.emitHtmlViewMessengerEvent(this.channelId,h,x)}sendCommand(h,x){return this.renderer.sendHtmlViewMessengerCommand(this.channelId,h,x)}on(h,x){let _=this.eventHandlers.get(h);return _===void 0&&(_=new Set,this.eventHandlers.set(h,_)),_.add(x),()=>{_?.delete(x)}}getHost(){return this.host}dispose(){this.unregisterReceiver(),this.eventHandlers.clear()}}t.DILHtmlViewMessengerApiImpl=l;function o(m,h){const x={channelId:m,command:h.command,event:h.event,registerTransport:h.registerTransport};return Object.freeze(s(x,x))}t.createDILHtmlViewMessengerHost=o;function u(m){const h=new Map,x=new Proxy(Object.create(null),{get:(e,c)=>{if(typeof c!="string"||c==="then")return;let f=h.get(c);return f===void 0&&(f=Object.freeze(g=>m.sendCommand(c,g)),h.set(c,f)),f}}),_=s({commands:x,emit:(e,c)=>{m.emit(e,c)},on:(e,c)=>{const f=m.on(e,c);return Object.freeze(()=>{f()})}},m.getHost());return(0,n.hardenApiFacade)(_)}t.createDILHtmlViewMessengerApiFacade=u;function d(m){if(!((typeof m!="object"||m===null)&&typeof m!="function"))return m[r]}t.getDILHtmlViewMessengerHost=d;function s(m,h){return new Proxy(m,{get:(x,_,e)=>_===r?h:Reflect.get(x,_,e)})}})(gt,gt.exports)),gt.exports}var Ze=Ht(),yt={exports:{}},vt={exports:{}},It={exports:{}},bt={exports:{}},ae;function qt(){return ae||(ae=1,(function(a,t){a.path="dil_renderer/src/DILRuntimeUtils",Object.defineProperty(t,"__esModule",{value:!0}),t.toError=t.isRecord=t.isPromiseLike=void 0;function n(o){return o!==null&&typeof o=="object"&&typeof o.then=="function"}t.isPromiseLike=n;function r(o){return o!==null&&typeof o=="object"}t.isRecord=r;function l(o){return o instanceof Error?o:new Error(String(o))}t.toError=l})(bt,bt.exports)),bt.exports}var le;function de(){return le||(le=1,(function(a,t){a.path="dil_renderer/src/DILHookRuntime",Object.defineProperty(t,"__esModule",{value:!0}),t.runCleanup=t.areDependenciesEqual=void 0;const n=qt();function r(o,u){if(o.length!==u.length)return!1;for(let d=0;d<o.length;d++)if(!Object.is(o[d],u[d]))return!1;return!0}t.areDependenciesEqual=r;function l(o,u){try{o()}catch(d){u.reportError((0,n.toError)(d))}}t.runCleanup=l})(It,It.exports)),It.exports}var xt={exports:{}},Rt={exports:{}},Ct={exports:{}},ue;function Bt(){return ue||(ue=1,(function(a,t){a.path="dil_renderer/src/DILSpeechSynthesis",Object.defineProperty(t,"__esModule",{value:!0}),t.sanitizeDILSpeechState=t.normalizeDILSpeechSource=t.UNAVAILABLE_DIL_SPEECH=t.normalizeDILSpeechRequestLog=t.DILSpeechFailureStage=t.DILSpeechStatus=void 0;var n;(function(h){h.Idle="idle",h.Loading="loading",h.Playing="playing"})(n||(t.DILSpeechStatus=n={}));var r;(function(h){h.Play="play",h.Request="request",h.Response="response",h.Streaming="streaming"})(r||(t.DILSpeechFailureStage=r={}));const l=console,o=["event_name","statsig_event_name","structured_event_name"];function u(h){try{if(h===null||typeof h!="object"||Array.isArray(h))return;const x=h,_={};for(const c of o){const f=x[c];if(f!==void 0){if(typeof f!="string"||!f.trim()||f.length>256)return;_[c]=f}}if(Object.keys(_).length===0)return;const e=x.event_data;if(e!==void 0){if(e===null||typeof e!="object"||Array.isArray(e))return;const c=Object.keys(e);if(c.length>32)return;const f=Object.create(null);for(const g of c){const w=e[g];if(g.length>128||!(w===null||typeof w=="boolean"||typeof w=="string"&&w.length<=2048||typeof w=="number"&&Number.isFinite(w)))return;f[g]=w}_.event_data=f}if(JSON.stringify(_).length<=16384)return _}catch{l.warn("DIL speech logging metadata could not be read")}}t.normalizeDILSpeechRequestLog=u,t.UNAVAILABLE_DIL_SPEECH=Object.freeze({status:n.Idle,available:!1,error:null});const d={language:128,pronunciationHint:4096,version:256,source:128};function s(h){if(h===null||typeof h!="object")return null;const x=h,_=x.text;if(typeof _!="string"||!_.trim()||_.length>32768)return null;const e={text:_};for(const f of Object.keys(d)){const g=x[f];if(g===void 0)continue;const w=d[f];if(typeof g!="string"||g.length>w)return null;(f!=="version"||g!=="")&&(e[f]=g)}const c=x.playbackSpeed;return typeof c=="number"&&Number.isFinite(c)&&c>=.25&&c<=4&&(e.playbackSpeed=c),e}t.normalizeDILSpeechSource=s;function m(h){if(h===null||typeof h!="object")return t.UNAVAILABLE_DIL_SPEECH;const x=h;let _=null;if(x.error!==null&&typeof x.error=="object"){const e=x.error,c=e.failureStage;if(c===r.Play||c===r.Request||c===r.Response||c===r.Streaming){const f=e.httpStatus;_=Object.assign({failureStage:c},typeof f=="number"&&Number.isInteger(f)&&f>=100&&f<=599?{httpStatus:f}:{})}}return{available:x.available===!0,status:_!==null?n.Idle:x.status===n.Loading||x.status===n.Playing?x.status:n.Idle,error:_}}t.sanitizeDILSpeechState=m})(Ct,Ct.exports)),Ct.exports}var ce;function tn(){return ce||(ce=1,(function(a,t){a.path="dil_renderer/src/DILSpeechOwner",Object.defineProperty(t,"__esModule",{value:!0}),t.DILSpeechOwner=void 0;const n=ot(),r=Bt(),l=console;class o{constructor(d,s,m,h){if(this.sourceKey=d,this.runtime=m,this.scheduleRender=h,this.disposed=!1,this.state=r.UNAVAILABLE_DIL_SPEECH,this.logging={},this.play=()=>{var x;if(!(this.disposed||this.session===void 0||!this.state.available||this.state.status!==r.DILSpeechStatus.Idle||!(!((x=this.runtime)===null||x===void 0)&&x.isUserInteraction())))try{this.session.play(this.logging.play_requested)}catch{this.fail(r.DILSpeechFailureStage.Play)}},this.stop=()=>{var x;if(!(this.disposed||this.session===void 0))try{this.session.stop(this.logging.stop_requested!==void 0&&this.state.available&&((x=this.runtime)===null||x===void 0?void 0:x.isUserInteraction())===!0?this.logging.stop_requested:void 0)}catch{this.fail(r.DILSpeechFailureStage.Play)}},!(s===null||m===void 0))try{const x=m.createSession(s,_=>this.receive(_));this.disposed?x.dispose():this.session=x}catch{this.state=r.UNAVAILABLE_DIL_SPEECH,this.fail(r.DILSpeechFailureStage.Request),this.disposed=!0}}setLogging(d){this.logging={};try{this.logging={play_requested:(0,r.normalizeDILSpeechRequestLog)(d?.play_requested),stop_requested:(0,r.normalizeDILSpeechRequestLog)(d?.stop_requested)}}catch{l.warn("DIL speech logging metadata could not be read")}}snapshot(){return(0,n.hardenApiFacade)(Object.assign(Object.assign({},this.state),{play:this.play,stop:this.stop}))}dispose(){if(this.disposed)return;this.disposed=!0;const d=this.session;this.session=void 0;try{d?.dispose()}catch{l.warn("DIL speech session disposal failed")}}receive(d){this.disposed||(this.state=(0,r.sanitizeDILSpeechState)(d),this.scheduleRender())}fail(d){l.warn(\`DIL speech session failed during \${d}\`),this.receive(Object.assign(Object.assign({},this.state),{status:r.DILSpeechStatus.Idle,error:{failureStage:d}}))}}t.DILSpeechOwner=o})(Rt,Rt.exports)),Rt.exports}var Et={exports:{}},wt={exports:{}},pe;function _e(){return pe||(pe=1,(function(a,t){a.path="dil_renderer/src/DILDictation",Object.defineProperty(t,"__esModule",{value:!0}),t.normalizeDILDictationOptions=t.DILDictationEventKind=t.DILDictationStatus=void 0;var n;(function(o){o.Idle="idle",o.Starting="starting",o.Recording="recording",o.Stopping="stopping",o.Done="done",o.Error="error"})(n||(t.DILDictationStatus=n={}));var r;(function(o){o.Recording="recording",o.Transcript="transcript",o.Stopping="stopping",o.Done="done",o.Error="error",o.Cancelled="cancelled"})(r||(t.DILDictationEventKind=r={}));function l(o){if(o===null||typeof o!="object"||Array.isArray(o))return null;const u=o.language;return u===void 0?{}:typeof u!="string"||!u.trim()||u.length>128?null:{language:u}}t.normalizeDILDictationOptions=l})(wt,wt.exports)),wt.exports}var me;function en(){return me||(me=1,(function(a,t){a.path="dil_renderer/src/DILDictationOwner",Object.defineProperty(t,"__esModule",{value:!0}),t.DILDictationOwner=void 0;const n=ot(),r=_e(),l=console,o=Object.freeze({status:r.DILDictationStatus.Idle,transcript:"",error:null});class u{constructor(s,m){this.runtime=s,this.scheduleRender=m,this.disposed=!1,this.generation=0,this.state=o,this.start=()=>{if(this.disposed||this.attempt!==void 0)return;const h=this.runtime;if(h===void 0){this.update(Object.assign(Object.assign({},o),{status:r.DILDictationStatus.Error,error:{code:"integration_error",message:"The dictation host service is not configured."}}));return}if(!h.isUserInteraction())return;const x={generation:++this.generation,session:void 0,startInvoked:!1,stopSent:!1};this.attempt=x,this.state=Object.assign(Object.assign({},o),{status:r.DILDictationStatus.Starting});let _;try{_=(0,r.normalizeDILDictationOptions)(this.options===void 0?{}:this.options)}catch{_=null}if(_===null){this.finish(x,{code:"invalid_options",message:"Dictation language must be a nonempty string of at most 128 characters."},void 0);return}if(this.scheduleRender(),!!this.isCurrent(x))try{const e=h.createSession(_,c=>this.receive(x,c));if(!this.isCurrent(x)){this.disposeSession(e);return}x.session=e,x.startInvoked=!0,e.start(),this.sendStop(x)}catch{this.commandFailed(x)}},this.stop=()=>{const h=this.attempt;h===void 0||this.state.status===r.DILDictationStatus.Stopping||(this.update(Object.assign(Object.assign({},this.state),{status:r.DILDictationStatus.Stopping})),this.sendStop(h))},this.cancel=()=>{if(this.disposed||this.attempt===void 0&&this.state===o)return;const h=this.invalidate();if(this.state=o,h?.session!==void 0){try{h.session.cancel()}catch{l.warn("DIL dictation cancellation failed")}this.disposeSession(h.session)}this.scheduleRender()}}setOptions(s){this.options=s}snapshot(){return(0,n.hardenApiFacade)(Object.assign(Object.assign({},this.state),{start:this.start,stop:this.stop,cancel:this.cancel}))}sendStop(s){if(!(!this.isCurrent(s)||this.state.status!==r.DILDictationStatus.Stopping||!s.startInvoked||s.session===void 0||s.stopSent)){s.stopSent=!0;try{s.session.stop()}catch{this.commandFailed(s)}}}dispose(){if(this.disposed)return;this.disposed=!0,this.options=void 0;const s=this.invalidate();this.state=o,s?.session!==void 0&&this.disposeSession(s.session)}receive(s,m){if(this.isCurrent(s))switch(m.kind){case r.DILDictationEventKind.Recording:this.state.status===r.DILDictationStatus.Starting&&this.update(Object.assign(Object.assign({},this.state),{status:r.DILDictationStatus.Recording}));break;case r.DILDictationEventKind.Transcript:this.update(Object.assign(Object.assign({},this.state),{transcript:m.transcript}));break;case r.DILDictationEventKind.Stopping:s.stopSent=!0,this.update(Object.assign(Object.assign({},this.state),{status:r.DILDictationStatus.Stopping}));break;case r.DILDictationEventKind.Done:this.finish(s,null,m.transcript);break;case r.DILDictationEventKind.Error:this.finish(s,{code:m.error.code,message:m.error.message},void 0);break;case r.DILDictationEventKind.Cancelled:this.invalidate(),this.state=o,s.session!==void 0&&this.disposeSession(s.session),this.scheduleRender();break}}finish(s,m,h){this.isCurrent(s)&&(this.invalidate(),this.state={status:m===null?r.DILDictationStatus.Done:r.DILDictationStatus.Error,transcript:h??this.state.transcript,error:m},s.session!==void 0&&this.disposeSession(s.session),this.scheduleRender())}commandFailed(s){l.warn("DIL dictation host command failed"),this.finish(s,{code:"host_error",message:"Dictation could not complete. Please try again."},void 0)}invalidate(){const s=this.attempt;return this.attempt=void 0,this.generation++,s}isCurrent(s){return!this.disposed&&s.generation===this.generation}update(s){this.state=s,this.scheduleRender()}disposeSession(s){try{s.dispose()}catch{l.warn("DIL dictation session disposal failed")}}}t.DILDictationOwner=u})(Et,Et.exports)),Et.exports}var fe;function nn(){return fe||(fe=1,(function(a,t){a.path="dil_renderer/src/DILRuntimeApiImpl",Object.defineProperty(t,"__esModule",{value:!0}),t.DILRuntimeApiImpl=void 0;const n=de(),r=Ht(),l=tn(),o=en(),u=Bt(),d=st(),s=setInterval.bind(globalThis),m=clearInterval.bind(globalThis);class h{constructor(_,e,c,f,g){this.htmlViewMessengerRuntime=_,this.host=e,this.statePersistence=c,this.speechSynthesis=f,this.dictation=g,this.activeBreakpoints=[],this.appData={},this.constants={},this.theme="light",this.timeSubscriptions=new Map}get renderedRootValue(){return this.rootValue}clearRenderedRootValue(){this.rootValue=void 0}updateVariables(_,e,c,f){if(this.hostIsMobile===void 0){const g=_.__dilHost;this.hostIsMobile=g!==null&&typeof g=="object"&&!Array.isArray(g)&&g.isMobile===!0}this.appData=_,this.constants=e,this.theme=c,this.activeBreakpoints=f}render(_){var e;if(this.rootValue=_,(0,d.isDILRuntimeElement)(_)&&typeof _.type=="function"&&typeof((e=_.props)===null||e===void 0?void 0:e.key)=="string"&&/^body:\\d+$/.test(_.props.key)){const f=Object.assign({},_.props);delete f.key,this.rootValue=(0,d.createDilElement)(_.type,f,_.children)}return _}useAppData(_){return _===void 0?this.appData:_(this.appData)}useConstants(_){return _===void 0?this.constants:_(this.constants)}useCallback(_,e){return this.useMemo(()=>_,e)}useEffect(_,e){const c=this.host.requireCurrentHookFrame("useEffect");let f=this.readHook(c,0);if(f===void 0&&(f={kind:0,cleanup:void 0,deps:void 0},c.hooks[c.hookIndex-1]=f),e===void 0||f.deps===void 0||!(0,n.areDependenciesEqual)(f.deps,e)){const g=f.deps;f.deps=e,this.host.enqueuePendingEffect({frame:c,hook:f,effect:_,previousDeps:g})}}useId(){const _=this.host.requireCurrentHookFrame("useId"),e=this.readHook(_,1);if(e!==void 0)return e.value;const c=\`dil-\${_.id}-\${_.hookIndex-1}\`;return _.hooks[_.hookIndex-1]={kind:1,value:c},c}useDILMessenger(_){return this.useHtmlViewMessengerHook("useDILMessenger",_)}useHtmlViewMessenger(_){return this.useHtmlViewMessengerHook("useHtmlViewMessenger",_)}useHtmlViewMessengerHook(_,e){const c=this.host.requireCurrentHookFrame(_),f=this.readHook(c,3);if(f!==void 0)return f.messenger.updateOptions(e),f.publicApi;const g=new r.DILHtmlViewMessengerApiImpl(this.htmlViewMessengerRuntime,e),w=(0,r.createDILHtmlViewMessengerApiFacade)(g);return c.hooks[c.hookIndex-1]={kind:3,messenger:g,publicApi:w},w}useMemo(_,e){const c=this.host.requireCurrentHookFrame("useMemo"),f=this.readHook(c,2);if(f!==void 0&&(0,n.areDependenciesEqual)(f.deps,e))return f.value;const g=_();return f===void 0?c.hooks[c.hookIndex-1]={kind:2,value:g,deps:e}:(f.value=g,f.deps=e),g}useNow(_,e){const c=_===void 0?!0:_;if(typeof c!="boolean")throw new TypeError("DIL.useNow expects a boolean");const f=e===void 0?1e3:e;if(f!==50&&f!==1e3)throw new TypeError("DIL.useNow expects intervalMs to be 50 or 1000");const g=this.host.requireCurrentHookFrame("useNow");return this.useEffect(()=>{var w;if(!(!c||!g.isMounted))return this.timeSubscriptions.set(f,((w=this.timeSubscriptions.get(f))!==null&&w!==void 0?w:0)+1),this.updateTimeInterval(),()=>{var P;const O=((P=this.timeSubscriptions.get(f))!==null&&P!==void 0?P:0)-1;O<=0?this.timeSubscriptions.delete(f):this.timeSubscriptions.set(f,O),this.updateTimeInterval()}},[c,f]),Date.now()}updateTimeInterval(){const _=this.timeSubscriptions.has(50)?50:this.timeSubscriptions.has(1e3)?1e3:void 0;_!==this.timeIntervalMs&&(this.timeInterval!==void 0&&(m(this.timeInterval),this.timeInterval=void 0),this.timeIntervalMs=_,_!==void 0&&(this.timeInterval=s(()=>this.host.scheduleRender(),_)))}useRef(_){const e=this.host.requireCurrentHookFrame("useRef"),c=this.readHook(e,4);if(c!==void 0)return c.ref;const f={current:_};return e.hooks[e.hookIndex-1]={kind:4,ref:f},f}useDictation(_){const e=this.host.requireCurrentHookFrame("useDictation");let c=this.readHook(e,7);return c===void 0&&(c={kind:7,owner:new o.DILDictationOwner(this.dictation,()=>{e.isMounted&&this.host.scheduleRender()})},e.hooks[e.hookIndex-1]=c),c.owner.setOptions(_),c.owner.snapshot()}useSpeechSynthesizer(_,e){const c=this.host.requireCurrentHookFrame("useSpeechSynthesizer"),f=(0,u.normalizeDILSpeechSource)(_),g=JSON.stringify(f);let w=this.readHook(c,6);return(w===void 0||w.owner.sourceKey!==g)&&(w?.owner.dispose(),w={kind:6,owner:new l.DILSpeechOwner(g,f,this.speechSynthesis,()=>{c.isMounted&&this.host.scheduleRender()})},c.hooks[c.hookIndex-1]=w),w.owner.setLogging(e),w.owner.snapshot()}useState(_,e){const c=this.host.requireCurrentHookFrame("useState"),f=this.readHook(c,5);if(f!==void 0)return[f.value,f.setter];const g=typeof _=="function"?_():_,w=this.statePersistence.createStateHook(c,g,e);return c.hooks[c.hookIndex-1]=w,[w.value,w.setter]}useTheme(){return{isDark:this.theme==="dark",isLight:this.theme==="light",theme:this.theme}}useBreakpoint(_){return this.activeBreakpoints.includes(_)}useIsMobile(){return this.hostIsMobile===!0}readHook(_,e){const c=_.hooks[_.hookIndex];if(_.hookIndex++,c!==void 0){if(c.kind!==e)throw new Error("DIL hooks must be called in the same order on every render.");return c}}}t.DILRuntimeApiImpl=h})(xt,xt.exports)),xt.exports}var Dt={exports:{}},St={exports:{}},he;function on(){return he||(he=1,(function(a,t){a.path="dil_renderer/src/DILStateSnapshotCodec",Object.defineProperty(t,"__esModule",{value:!0}),t.removeDILStateSnapshotValue=t.setDILStateSnapshotValue=t.parseDILStateSnapshot=t.DIL_WIDGET_STATE_MAX_ENTRIES=t.DIL_ROOT_STATE_MAX_ENTRIES=void 0;const n=8*1024,r=8;t.DIL_ROOT_STATE_MAX_ENTRIES=128,t.DIL_WIDGET_STATE_MAX_ENTRIES=512;function l(e,c){try{return d(JSON.parse(e),c)}catch{return null}}t.parseDILStateSnapshot=l;function o(e,c,f,g){const w=s(e?.state);return w[c]=f,d(w,g)}t.setDILStateSnapshotValue=o;function u(e,c,f){const g=s(e?.state);return delete g[c],d(g,f)}t.removeDILStateSnapshotValue=u;function d(e,c){try{if(!x(e)||!m(e,0,new WeakSet,{entries:0,maxEntries:c}))return null;const f=JSON.stringify(e);if(new TextEncoder().encode(f).byteLength>n)return null;const g=JSON.parse(f);return x(g)?{serialized:f,state:g}:null}catch{return null}}function s(e){const c=Object.create(null);if(e!==void 0)for(const f of Object.keys(e))c[f]=e[f];return c}function m(e,c,f,g){if(c>r)return!1;if(e===null||typeof e=="boolean")return!0;if(typeof e=="string")return!_(e);if(typeof e=="number")return Number.isFinite(e);if(typeof e!="object"||f.has(e))return!1;f.add(e);try{if(Array.isArray(e)){if(Object.getPrototypeOf(e)!==Array.prototype||e.length>g.maxEntries-g.entries||Reflect.ownKeys(e).length!==e.length+1)return!1;for(let w=0;w<e.length;w++)if(!h(e,String(w),c,f,g))return!1;return!0}if(!x(e))return!1;for(const w of Reflect.ownKeys(e))if(typeof w!="string"||_(w)||!h(e,w,c,f,g))return!1;return!0}finally{f.delete(e)}}function h(e,c,f,g,w){const P=Object.getOwnPropertyDescriptor(e,c);return P===void 0||!P.enumerable||P.value===void 0?!1:(w.entries++,w.entries<=w.maxEntries&&m(P.value,f+1,g,w))}function x(e){if(e===null||typeof e!="object"||Array.isArray(e))return!1;const c=Object.getPrototypeOf(e);return c===Object.prototype||c===null}function _(e){try{return encodeURIComponent(e),!1}catch{return!0}}})(St,St.exports)),St.exports}var ge;function rn(){return ge||(ge=1,(function(a,t){a.path="dil_renderer/src/DILStatePersistence",Object.defineProperty(t,"__esModule",{value:!0}),t.DILStatePersistence=t.DILStateSnapshotChangeOrigin=void 0;const n=on();var r;(function(o){o[o.Initial=0]="Initial",o[o.LocalAction=1]="LocalAction"})(r||(t.DILStateSnapshotChangeOrigin=r={}));class l{get maxSnapshotEntries(){return this.widgetId===void 0?n.DIL_ROOT_STATE_MAX_ENTRIES:n.DIL_WIDGET_STATE_MAX_ENTRIES}constructor(u,d,s){this.scheduleRender=u,this.stateChangeHandler=d,this.stateSynchronizationErrorHandler=s,this.widgetScopes=new Map,this.allocatedSlots=new Set,this.allocationByOwner=new Map,this.nextAnonymousSlot=0,this.pendingInitialSnapshot=!1}createStateHook(u,d,s){var m;if(s?.widgetId!==void 0)return this.widgetScope(s.widgetId).createStateHook(u,d,{key:s.key});const h=s?.key;if(h!==void 0&&(typeof h!="string"||h.length===0))throw new Error("DIL state keys must be nonempty strings.");const x=u.stateIdentity,_=x?.nextStateSlot;x!==void 0&&x.nextStateSlot++;const e=u.hookIndex-1,c=x===void 0?\`\${u.id}:\${e}\`:\`\${h??_}\\0\${x.ownerKey}\`,f=this.resolveSlot(c,h,this.widgetId===void 0?x?.siblingOrdinal:void 0,x!==void 0),g=(m=this.snapshot)===null||m===void 0?void 0:m.state[f],w=g===void 0?d:g,P={kind:5,defaultValue:d,ownerId:c,slot:f,widgetId:this.widgetId,value:w,setter:()=>{}};if(P.setter=O=>{const V=typeof O=="function"?O(P.value):O;Object.is(P.value,V)||(P.value=V,this.updatePersistedHookValue(P,V),this.scheduleRender())},g===void 0){const O=(0,n.setDILStateSnapshotValue)(this.snapshot,f,d,this.maxSnapshotEntries);O!==null&&(this.snapshot=O,this.pendingInitialSnapshot=!0)}return P}releaseStateHook(u){if(u.widgetId!==this.widgetId&&u.widgetId!==void 0){this.widgetScope(u.widgetId).releaseStateHook(u);return}const d=this.allocationByOwner.get(u.ownerId);d===void 0||d.retainAfterUnmount||(this.allocationByOwner.delete(u.ownerId),this.allocatedSlots.delete(d.slot))}setStateSnapshot(u,d){var s;if(d!==void 0)return this.widgetScope(d).setStateSnapshot(u);const m=u===void 0?void 0:(0,n.parseDILStateSnapshot)(u,this.maxSnapshotEntries);return m===null||m?.serialized===((s=this.snapshot)===null||s===void 0?void 0:s.serialized)?!1:(this.snapshot=m,this.pendingInitialSnapshot=!1,!0)}applySnapshotToHook(u){var d;if(u.widgetId!==this.widgetId&&u.widgetId!==void 0)return this.widgetScope(u.widgetId).applySnapshotToHook(u);const s=(d=this.snapshot)===null||d===void 0?void 0:d.state[u.slot],m=s===void 0?u.defaultValue:s;return Object.is(u.value,m)?!1:(u.value=m,!0)}commitStateSnapshot(){for(const u of this.widgetScopes.values())u.commitStateSnapshot();!this.pendingInitialSnapshot||this.snapshot===void 0||(this.pendingInitialSnapshot=!1,this.notifyStateChange(this.snapshot.serialized,r.Initial))}updateRenderedRootIdentity(u){for(const d of this.widgetScopes.values())d.updateRenderedRootIdentity(u);if(this.renderedRootIdentity===void 0){this.renderedRootIdentity=u;return}this.renderedRootIdentity!==u&&(this.renderedRootIdentity=u,this.allocationByOwner.clear(),this.allocatedSlots.clear(),this.nextAnonymousSlot=0)}resolveSlot(u,d,s,m){const h=this.allocationByOwner.get(u);if(h!==void 0)return h.slot;let x;if(d===void 0)do x=String(this.nextAnonymousSlot),this.nextAnonymousSlot++;while(this.allocatedSlots.has(x));else{x=d;let _=s??1;for(_>1&&(x=\`\${d}:\${_}\`);this.allocatedSlots.has(x);)_++,x=\`\${d}:\${_}\`}return this.allocationByOwner.set(u,{retainAfterUnmount:m,slot:x}),this.allocatedSlots.add(x),x}updatePersistedHookValue(u,d){var s,m;let h=(0,n.setDILStateSnapshotValue)(this.snapshot,u.slot,d,this.maxSnapshotEntries);h===null&&(((s=this.snapshot)===null||s===void 0?void 0:s.state[u.slot])===void 0||(h=(0,n.removeDILStateSnapshotValue)(this.snapshot,u.slot,this.maxSnapshotEntries),h===null))||h.serialized!==((m=this.snapshot)===null||m===void 0?void 0:m.serialized)&&(this.snapshot=h,this.notifyStateChange(h.serialized,r.LocalAction))}notifyStateChange(u,d){if(this.stateChangeHandler!==void 0)try{this.stateChangeHandler(u,d,this.widgetId)}catch(s){this.stateSynchronizationErrorHandler(s instanceof Error?s:new Error(String(s)))}}widgetScope(u){if(typeof u!="string"||!u||u.length>1024)throw new Error("DIL widget state requires a valid widget identity.");let d=this.widgetScopes.get(u);return d===void 0&&(d=new l(this.scheduleRender,this.stateChangeHandler,this.stateSynchronizationErrorHandler),d.widgetId=u,d.renderedRootIdentity=this.renderedRootIdentity,this.widgetScopes.set(u,d)),d}}t.DILStatePersistence=l})(Dt,Dt.exports)),Dt.exports}var kt={exports:{}},ye;function ve(){return ye||(ye=1,(function(a,t){a.path="dil_renderer/src/DILSourceEvaluator",Object.defineProperty(t,"__esModule",{value:!0}),t.createDILRuntimeApiFacade=t.createDILDateConstructor=t.createDILJsxRuntimeFacade=void 0;const n=st(),r=ot();function l(d){return(0,r.hardenApiFacade)((0,n.createDilJsxRuntime)(d))}t.createDILJsxRuntimeFacade=l;function o(d){if(d===void 0)return Date;const s=function(...m){return new.target===void 0?new Date(d).toString():Reflect.construct(Date,m.length===0?[d]:m,new.target)};return Object.setPrototypeOf(s,Date),Object.defineProperty(s,"prototype",{value:Date.prototype}),Object.defineProperty(s,"now",{configurable:!1,enumerable:!1,value:Object.freeze(()=>d),writable:!1}),Object.freeze(s)}t.createDILDateConstructor=o;function u(d){const s={render(m){return d.render(m)},useAppData(m){return d.useAppData(m)},useConstants(m){return d.useConstants(m)},useCallback(m,h){return d.useCallback(m,h)},useEffect(m,h){d.useEffect(m,h)},useId(){return d.useId()},useDILMessenger(m){return d.useDILMessenger(m)},useHtmlViewMessenger(m){return d.useHtmlViewMessenger(m)},useMemo(m,h){return d.useMemo(m,h)},useNow(m,h){return d.useNow(m,h)},useRef(m){return d.useRef(m)},useDictation(m){return d.useDictation(m)},useSpeechSynthesizer(m,h){return d.useSpeechSynthesizer(m,h)},useState(m,h){return d.useState(m,{key:h?.key,widgetId:void 0})},useTheme(){return d.useTheme()},useBreakpoint(m){return d.useBreakpoint(m)},useIsMobile(){return d.useIsMobile()}};return(0,r.hardenApiFacade)(s)}t.createDILRuntimeApiFacade=u})(kt,kt.exports)),kt.exports}var Ut={exports:{}},Ie;function sn(){return Ie||(Ie=1,(function(a,t){a.path="dil_renderer/src/DILSemanticKey",Object.defineProperty(t,"__esModule",{value:!0}),t.getSinglePotentialHostRootIndex=void 0;const n=st();function r(o){let u=-1;for(let d=0;d<o.length;d++){const s=l(o[d]);if(s!==0){if(s>1||u!==-1)return-1;u=d}}return u}t.getSinglePotentialHostRootIndex=r;function l(o){if(o==null||typeof o=="boolean"||o==="")return 0;const u=Array.isArray(o)?o:(0,n.isDILRuntimeElement)(o)&&o.type===n.DIL_FRAGMENT?o.children:void 0;if(u===void 0)return 1;let d=0;for(const s of u)if(d+=l(s),d>1)return 2;return d}})(Ut,Ut.exports)),Ut.exports}var be;function an(){return be||(be=1,(function(a,t){a.path="dil_renderer/src/DILRenderFunctionRuntime",Object.defineProperty(t,"__esModule",{value:!0}),t.DILRenderFunctionRuntime=void 0;const n=st(),r=de(),l=$t(),o=nn(),u=rn(),d=ve(),s=ot(),m=qt(),h=sn(),x=Symbol("openai.valdi.dil.componentFunctionId"),_=Object.freeze([]);let e=0;class c{constructor(i,p,y,b,E,k,L,j,z,W,A,q,F,B){this.elementsMetadata=i,this.delegate=p,this.errorReporter=y,this.errorMode=b,this.scheduler=k,this.trace=L,this.dateConstructor=j,this.fixedTimeMs=z,this.genUIFacade=W,this.committedRootChildren=[],this.nextFrameId=0,this.nextNodeId=1,this.nodeStack=[],this.pendingDestroyedNodes=[],this.renderId=0,this.renderFailed=!1,this.sourceGeneration=0,this.rootNode={key:"root",kind:4,lastRenderId:0,parentIndex:0},this.nextRootChildren=[],this.namedFunctionIdentities=new Map,this.keyedComponents=new Map,this.renderedParentStack=[],this.resolvedComponents=Object.create(null),this.resolvedComponentScopes=new WeakMap,this.isFlushingEffects=!1,this.statePersistence=new u.DILStatePersistence(()=>this.scheduleRender(),A,v=>this.errorReporter.reportError(v)),this.runtimeApi=new o.DILRuntimeApiImpl(E,this,this.statePersistence,q,F===void 0?void 0:{createSession:(v,I)=>F.createSession(v,I),isUserInteraction:()=>this.nodeStack.length===0&&!this.isFlushingEffects&&F.isUserInteraction()}),this.runtimeApiFacade=(0,d.createDILRuntimeApiFacade)(this.runtimeApi),this.widgetRuntimeApiFacade=(0,s.hardenApiFacade)(Object.assign(Object.assign({},this.runtimeApiFacade),{useState:(v,I)=>this.runtimeApi.useState(v,I)})),this.modelGenUIFacade=W===void 0?void 0:(0,s.createDILModelGenUIFacade)(W),this.jsxRuntimeFacade=(0,d.createDILJsxRuntimeFacade)(v=>this.registerKeyedComponent(v)),this.sourceEvaluator=B({dateConstructor:this.dateConstructor,fixedTimeMs:this.fixedTimeMs,genUIFacade:this.genUIFacade,jsxRuntimeFacade:this.jsxRuntimeFacade,modelGenUIFacade:this.modelGenUIFacade,reportUnhandledError:v=>{this.errorReporter.reportUnhandledError!==void 0?this.errorReporter.reportUnhandledError(v):this.errorReporter.reportError(v)},runtimeApiFacade:this.runtimeApiFacade,widgetRuntimeApiFacade:this.widgetRuntimeApiFacade,trace:this.trace})}evaluate(i){this.sourceGeneration++,this.runtimeApi.clearRenderedRootValue();try{this.sourceEvaluator.evaluate(i)}catch(p){this.runtimeApi.clearRenderedRootValue(),this.reportError(p)}}clearRenderedRoot(){this.runtimeApi.clearRenderedRootValue()}setStateSnapshot(i,p){return this.statePersistence.setStateSnapshot(i,p)?(this.applyStateSnapshotToMountedHooks(this.rootNode,p)&&this.scheduleRender(),!0):!1}commitStateSnapshot(){this.statePersistence.commitStateSnapshot()}didLastRenderFail(){return this.renderFailed}render(i,p,y,b){this.runtimeApi.updateVariables(i,p,y,b),this.pendingEffects=void 0,this.renderFailed=!1,this.unmountCleanups=void 0,this.nodeStack.length=0,this.renderedParentStack.length=0,this.renderedParentStack.push(void 0),this.renderId++,this.pushRenderNode(this.rootNode);try{this.sourceEvaluator.beginRender();const E=this.runtimeApi.renderedRootValue;this.statePersistence.updateRenderedRootIdentity(this.getRenderedRootIdentity(E)),this.renderValue(E,"root",void 0)}catch(E){this.reportError(E)}finally{try{this.endRenderNode(this.rootNode)}catch(E){this.reportError(E)}if(!this.renderFailed)try{this.commitUpdates()}catch(E){this.reportError(E)}this.renderFailed&&this.discardPendingEffectsFrom(0),this.renderedParentStack.length=0,this.scheduleEffectFlush(void 0)}}dispose(){this.pendingEffects=void 0,this.unmountCleanups=void 0;const i=this.rootNode.children;if(i!==void 0){for(const p of i.children)this.destroyRenderNode(p);this.rootNode.children=void 0}this.runtimeApi.clearRenderedRootValue(),this.scheduleEffectFlush(()=>this.sourceEvaluator.dispose())}renderValue(i,p,y){if(!(i==null||typeof i=="boolean")){if(Array.isArray(i)){const b=this.beginRenderNode(0,\`array:\${p}\`);try{const E=y===void 0?-1:(0,h.getSinglePotentialHostRootIndex)(i);let k=0;for(const L of i)this.renderValue(L,P(L,k),k===E?y:void 0),k++}finally{this.endRenderNode(b)}return}if((0,n.isDILRuntimeElement)(i)){this.renderRuntimeElement(i,p,y);return}if(typeof i=="function"){const b=this.resolveComponent(i);this.renderComponent(b,void 0,[],this.getComponentNodeKey(b,p),void 0,void 0);return}this.renderText(String(i),\`t:\${p}\`)}}renderRuntimeElement(i,p,y){const b=i.type,E=(0,n.readRuntimeElementKey)(i),k=y??E;if(b===n.DIL_FRAGMENT){this.renderFragment(i.children,p,k);return}const L=E===void 0?p:\`k:\${typeof E}:\${String(E)}\`;if(typeof b=="function"){const j=this.resolveComponent(b);this.renderComponent(j,i.props,i.children,this.getComponentNodeKey(j,L),E===void 0?void 0:this.getComponentStateIdentity(j,E),k);return}this.renderElement(b,i.props,i.children,\`e:\${b}:\${L}\`,k)}renderFragment(i,p,y){const b=y===void 0?-1:(0,h.getSinglePotentialHostRootIndex)(i);let E=0;for(const k of i)this.renderValue(k,O(k,E,p),E===b?y:void 0),E++}renderChildren(i){let p=0;for(const y of i)this.renderValue(y,P(y,p),void 0),p++}renderComponent(i,p,y,b,E,k){var L,j,z;const W=this.beginRenderNode(1,b);W.hookFrame.stateIdentity=this.resolveComponentStateIdentity(E,W,b);const A=(j=(L=this.pendingEffects)===null||L===void 0?void 0:L.length)!==null&&j!==void 0?j:0,q=this.interactionScope;this.interactionScope=(z=this.resolvedComponentScopes.get(i))!==null&&z!==void 0?z:q;try{const F=i((0,n.propsWithChildren)(p,y));this.renderValue(F,"rendered",k)}catch(F){this.discardPendingEffectsFrom(A),this.reportSubtreeError(F)}finally{this.interactionScope=q,this.endRenderNode(W)}}resolveComponent(i){const p=(0,n.getDilComponentResolver)(i);if(p===void 0)return i;let y=this.resolvedComponents[i.name];if(y===void 0){y=p(this.widgetRuntimeApiFacade,this.jsxRuntimeFacade,this.genUIFacade),this.resolvedComponents[i.name]=y;const b=(0,n.getDilComponentScope)(i);b!==void 0&&this.resolvedComponentScopes.set(y,b)}return y}renderElement(i,p,y,b,E){var k;const L=this.elementsMetadata[i];if(L===void 0){this.reportSubtreeError(new Error(\`Unknown DIL element type "\${i}".\`));return}let j;try{j=L.renderedPropNames!==void 0&&p!==void 0?this.resolveRenderableProps(L.renderedPropNames,p):void 0}catch(W){this.reportSubtreeError(W);return}const z=this.beginRenderNode(2,b);try{let W=!0,A,q;switch(L.childrenMode){case l.DILElementChildrenMode.RenderedNodes:break;case l.DILElementChildrenMode.PrimitiveText:{q=V(y),W=q===void 0;break}case l.DILElementChildrenMode.ResolvedValues:break;case l.DILElementChildrenMode.OpaqueTree:W=!1,A=this.renderOpaqueChildren(y);break}const F=j===void 0?p:j.props;z.elementOutput={interactionScope:this.interactionScope,metadata:L,opaqueChildren:A,primitiveTextValue:q,props:L.semanticKeyPropName===void 0?F:Object.assign(Object.assign({},F),{[L.semanticKeyPropName]:E})};const B=this.getRenderedElement(z);B.lastResolvedRenderId=this.renderId,B.resolvedParent=this.renderedParentStack[this.renderedParentStack.length-1],this.renderedParentStack.push(B);try{if(j!==void 0)for(const v of j.elements)this.renderRuntimeElement(v,\`prop:\${JSON.stringify([(k=v.props)===null||k===void 0?void 0:k[n.DIL_RENDERED_PROP_NAME]])}\`,void 0);W&&this.renderChildren(y)}finally{this.renderedParentStack.pop()}}catch(W){this.reportSubtreeError(W)}finally{this.endRenderNode(z)}}resolveRenderableProps(i,p){let y;for(const b of i){const E=p[b];(0,n.isDILRuntimeElement)(E)&&(y=y??{elements:[],props:Object.assign({},p)},delete y.props[b],y.elements.push((0,n.createDilElement)(n.DIL_RENDERED_PROP_ELEMENT_TYPE,{[n.DIL_RENDERED_PROP_NAME]:b},[E])))}return y}renderText(i,p){const y=this.beginRenderNode(5,p);try{y.textOutput=i;const b=this.getRenderedText(y);b.lastResolvedRenderId=this.renderId,b.resolvedParent=this.renderedParentStack[this.renderedParentStack.length-1]}finally{this.endRenderNode(y)}}renderOpaqueChildren(i){const p=[];let y=0;for(const b of i)this.renderOpaqueValue(b,P(b,y),p,void 0),y++;return p}renderOpaqueValue(i,p,y,b){if(!(i==null||typeof i=="boolean")){if(Array.isArray(i)){const E=this.beginRenderNode(0,\`opaque-array:\${p}\`);try{const k=b===void 0?-1:(0,h.getSinglePotentialHostRootIndex)(i);let L=0;for(const j of i)this.renderOpaqueValue(j,P(j,L),y,L===k?b:void 0),L++}finally{this.endRenderNode(E)}return}if((0,n.isDILRuntimeElement)(i)){this.renderOpaqueRuntimeElement(i,p,y,b);return}if(typeof i=="function"){const E=this.resolveComponent(i);this.renderOpaqueComponent(E,void 0,[],this.getComponentNodeKey(E,p),void 0,y,void 0);return}if(typeof i=="string"||typeof i=="number"){y.push(i);return}y.push(String(i))}}renderOpaqueRuntimeElement(i,p,y,b){var E;const k=i.type,L=(0,n.readRuntimeElementKey)(i),j=b??L;if(k===n.DIL_FRAGMENT){this.renderOpaqueFragment(i.children,p,y,j);return}const z=L===void 0?p:\`k:\${typeof L}:\${String(L)}\`;if(typeof k=="function"){const A=this.resolveComponent(k);this.renderOpaqueComponent(A,i.props,i.children,this.getComponentNodeKey(A,z),L===void 0?void 0:this.getComponentStateIdentity(A,L),y,j);return}const W=this.beginRenderNode(3,\`opaque-element:\${k}:\${z}\`);try{const A=this.elementsMetadata[k],q=A?.semanticKeyPropName;let F=f(i.props);for(const B of(E=A?.renderedPropNames)!==null&&E!==void 0?E:[]){const v=F[B];if((0,n.isDILRuntimeElement)(v)){const I=[];this.renderOpaqueValue(v,\`opaque-prop:\${JSON.stringify(B)}\`,I,void 0),F=Object.assign(Object.assign({},F),{[B]:I.length===0?null:I.length===1?I[0]:I})}}y.push({type:k,props:q===void 0?F:Object.assign(Object.assign({},F),{[q]:j}),children:this.renderOpaqueChildren(i.children)})}finally{this.endRenderNode(W)}}renderOpaqueFragment(i,p,y,b){const E=b===void 0?-1:(0,h.getSinglePotentialHostRootIndex)(i);let k=0;for(const L of i)this.renderOpaqueValue(L,O(L,k,p),y,k===E?b:void 0),k++}renderOpaqueComponent(i,p,y,b,E,k,L){var j,z;const W=this.beginRenderNode(1,b);W.hookFrame.stateIdentity=this.resolveComponentStateIdentity(E,W,b);const A=k.length,q=(z=(j=this.pendingEffects)===null||j===void 0?void 0:j.length)!==null&&z!==void 0?z:0;try{const F=i((0,n.propsWithChildren)(p,y));this.renderOpaqueValue(F,"rendered",k,L)}catch(F){k.length=A,this.discardPendingEffectsFrom(q),this.reportSubtreeError(F)}finally{this.endRenderNode(W)}}reportSubtreeError(i){this.errorMode===l.DILRenderErrorMode.ReportAndAbortRender&&(this.renderFailed=!0),this.errorReporter.reportError((0,m.toError)(i))}reportError(i){this.renderFailed=!0,this.errorReporter.reportError((0,m.toError)(i))}getHookFrame(i){if(i.hookFrame!==void 0)return i.hookFrame;const p={id:this.nextFrameId,isMounted:!0,hookIndex:0,hooks:[],stateIdentity:void 0};return this.nextFrameId++,i.hookFrame=p,p}getFunctionId(i){var p;const y=i,b=y[x];if(b!==void 0)return b;const E=this.claimStableFunctionIdentity(i.name),k=(p=E?.id)!==null&&p!==void 0?p:e++;return Object.defineProperty(y,x,{value:k}),k}claimStableFunctionIdentity(i){if(i!==""){const y=this.namedFunctionIdentities.get(i);if(y===void 0){const b=this.createStableFunctionIdentity();return this.namedFunctionIdentities.set(i,b),b}return y.claimedGeneration!==this.sourceGeneration?(y.claimedGeneration=this.sourceGeneration,y):void 0}const p=this.firstAnonymousFunctionIdentity;if(p===void 0){const y=this.createStableFunctionIdentity();return this.firstAnonymousFunctionIdentity=y,y}if(p.claimedGeneration!==this.sourceGeneration)return p.claimedGeneration=this.sourceGeneration,p}createStableFunctionIdentity(){return{id:e++,claimedGeneration:this.sourceGeneration}}getComponentNodeKey(i,p){return\`c:\${this.getFunctionId(i)}:\${p}\`}getComponentStateIdentity(i,p){const y=\`\${typeof p}:\${String(p)}\`;let b=1;for(const[E,k]of this.keyedComponents)k===i&&\`\${typeof E}:\${String(E)}\`<y&&b++;return{key:p,nextStateSlot:0,ownerKey:y,siblingOrdinal:b}}resolveComponentStateIdentity(i,p,y){return i===void 0||p.key===y?i:{key:i.key,nextStateSlot:0,ownerKey:\`\${i.ownerKey}\\0\${p.key}\`,siblingOrdinal:i.siblingOrdinal}}registerKeyedComponent(i){const p=(0,n.readRuntimeElementKey)(i);p!==void 0&&typeof i.type=="function"&&this.keyedComponents.set(p,this.resolveComponent(i.type))}getRenderedRootIdentity(i){if((0,n.isDILRuntimeElement)(i)){const p=(0,n.readRuntimeElementKey)(i),y=p===void 0?"":\`\${typeof p}:\${String(p)}\`;return typeof i.type=="function"?\`component:\${this.getFunctionId(this.resolveComponent(i.type))}:\${y}\`:\`element:\${String(i.type)}:\${y}\`}return typeof i=="function"?\`component:\${this.getFunctionId(this.resolveComponent(i))}\`:Array.isArray(i)?"array":typeof i}requireCurrentHookFrame(i){const p=this.nodeStack[this.nodeStack.length-1],y=p?.hookFrame;if(y===void 0)throw new Error(\`DIL.\${i} can only be called while rendering a DIL component.\`);return y}beginRenderNode(i,p){const y=this.getCurrentRenderNode(),b=this.resolveRenderNode(y,i,p,void 0);if(i===1){const E=this.getHookFrame(b);E.hookIndex=0}else b.hookFrame=y.hookFrame;return this.pushRenderNode(b),b}pushRenderNode(i){i.children!==void 0&&(i.children.insertionIndex=0),i.lastRenderId=this.renderId,this.nodeStack.push(i)}endRenderNode(i){if(this.nodeStack.pop()!==i)throw new Error("Unbalanced DIL render node stack.");this.processChildrenUpdate(i)}getCurrentRenderNode(){const i=this.nodeStack[this.nodeStack.length-1];if(i===void 0)throw new Error("Unbalanced DIL render node stack.");return i}resolveRenderNode(i,p,y,b){const E=b===void 0?y:\`\${y}-\${b}\`;let k,L=i.children;if(L===void 0?(L={childByKey:{},children:[],insertionIndex:0},i.children=L):k=L.childByKey[E],k===void 0)k={key:E,kind:p,lastRenderId:this.renderId,parent:i,parentIndex:0},L.childByKey[E]=k;else if(k.kind!==p||k.lastRenderId===this.renderId){const j=L.insertionIndex-k.parentIndex+1;return this.resolveRenderNode(i,p,y,j)}return this.insertRenderNodeInParent(k,L),k}insertRenderNodeInParent(i,p){const y=p.insertionIndex;if(p.insertionIndex++,i.parentIndex=y,p.children!==p.previousChildren){p.children.push(i);return}if(p.children[y]!==i){const b=p.children.slice(0,y);b.push(i),p.children=b}}processChildrenUpdate(i){const p=i.children;if(p===void 0)return;if(p.children===p.previousChildren){const b=p.insertionIndex;if(b===p.children.length)return;p.children=p.children.slice(0,b)}const y=p.previousChildren;if(y!==void 0)for(const b of y)b.lastRenderId!==this.renderId&&(delete p.childByKey[b.key],this.destroyRenderNode(b));p.previousChildren=p.children}destroyRenderNode(i){const p=i.renderedNode;if(p?.committed&&this.pendingDestroyedNodes.push(p),i.kind===1&&i.hookFrame!==void 0){i.hookFrame.isMounted=!1;for(const b of i.hookFrame.hooks)b.kind===0&&b.cleanup!==void 0?(this.enqueueUnmountCleanup(b.cleanup),b.cleanup=void 0):b.kind===3?b.messenger.dispose():b.kind===6||b.kind===7?b.owner.dispose():b.kind===5&&this.statePersistence.releaseStateHook(b)}i.hookFrame=void 0;const y=i.children;if(y!==void 0){for(const b of y.children)this.destroyRenderNode(b);i.children=void 0}}applyStateSnapshotToMountedHooks(i,p){let y=!1;if(i.kind===1&&i.hookFrame!==void 0)for(const E of i.hookFrame.hooks)E.kind===5&&E.widgetId===p&&this.statePersistence.applySnapshotToHook(E)&&(y=!0);const b=i.children;if(b!==void 0)for(const E of b.children)this.applyStateSnapshotToMountedHooks(E,p)&&(y=!0);return y}commitUpdates(){for(const p of this.pendingDestroyedNodes)this.delegate.onNodeDestroyed(p.id),p.committed=!1;this.pendingDestroyedNodes.length=0,this.commitChildUpdates(void 0,this.committedRootChildren,this.nextRootChildren,this.rootNode);const i=this.committedRootChildren;this.committedRootChildren=this.nextRootChildren,this.nextRootChildren=i,this.nextRootChildren.length=0}commitChildUpdates(i,p,y,b){let E=0,k=0;this.forEachDirectRenderedChild(b,L=>{var j;const z=L.renderedNode;for(;E<p.length&&(p[E].lastResolvedRenderId!==this.renderId||p[E].resolvedParent!==i);)E++;if(this.emitRenderedNodeChanges(z,L),z!==p[E]?this.delegate.onNodeMoved(z.id,(j=i?.id)!==null&&j!==void 0?j:0,k):E++,y.push(z),z.kind===2){this.commitChildUpdates(z,z.children,z.nextChildren,L);const W=z.children;z.children=z.nextChildren,z.nextChildren=W,z.nextChildren.length=0}k++})}emitRenderedNodeChanges(i,p){var y,b,E;if(i.kind===5)this.emitRenderedTextChanges(i,p.textOutput);else{this.emitRenderedElementChanges(i,p.elementOutput);const k=(y=p.elementOutput)===null||y===void 0?void 0:y.interactionScope;i.interactionScope!==k&&((E=(b=this.delegate).onNodeInteractionScopeChanged)===null||E===void 0||E.call(b,i.id,k),i.interactionScope=k)}}emitRenderedElementChanges(i,p){var y;const b=i.metadata,E=p.props,k=b.childrenMode===l.DILElementChildrenMode.PrimitiveText?b.primitiveTextProp:void 0;if(!i.committed)this.delegate.onElementNodeCreated(i.id,b.typeId);else for(const j of i.propNames)j==="key"||j===k||E?.[j]===void 0&&i.props[j]!==void 0&&(this.delegate.onNodePropChanged(i.id,j,void 0),i.props[j]=void 0);const L=E===void 0?_:Object.keys(E);if(i.propNames=L,E!==void 0)for(const j of L){if(j==="key"||j===k)continue;const z=E[j];(!i.committed||i.props[j]!==z)&&(this.delegate.onNodePropChanged(i.id,j,z),i.props[j]=z)}if(b.childrenMode===l.DILElementChildrenMode.PrimitiveText){const j=b.primitiveTextProp,z=(y=p.primitiveTextValue)!==null&&y!==void 0?y:E?.[j];i.props[j]!==z&&this.delegate.onNodePropChanged(i.id,j,z),i.props[j]=z}if(b.childrenMode===l.DILElementChildrenMode.OpaqueTree){const j=p.opaqueChildren;(!i.committed||!g(i.opaqueChildren,j))&&this.delegate.onNodeOpaqueChildrenChanged(i.id,j),i.opaqueChildren=j}i.committed=!0}emitRenderedTextChanges(i,p){if(!i.committed){this.delegate.onTextNodeCreated(i.id,p),i.value=p,i.committed=!0;return}i.value!==p&&(this.delegate.onTextNodeChanged(i.id,p),i.value=p)}getRenderedElement(i){const p=i.renderedNode;if(p!==void 0)return p;const y={id:this.nextNodeId++,kind:2,metadata:i.elementOutput.metadata,children:[],committed:!1,lastResolvedRenderId:0,nextChildren:[],opaqueChildren:void 0,props:{},propNames:_,resolvedParent:void 0};return i.renderedNode=y,y}getRenderedText(i){const p=i.renderedNode;if(p!==void 0)return p;const y={id:this.nextNodeId++,kind:5,committed:!1,lastResolvedRenderId:0,resolvedParent:void 0,value:""};return i.renderedNode=y,y}forEachDirectRenderedChild(i,p){const y=i.children;if(y!==void 0)for(const b of y.children)b.kind===2||b.kind===5?p(b):this.forEachDirectRenderedChild(b,p)}scheduleRender(){this.scheduler.scheduleRender()}enqueuePendingEffect(i){this.pendingEffects===void 0&&(this.pendingEffects=[]),this.pendingEffects.push(i)}discardPendingEffectsFrom(i){if(this.pendingEffects!==void 0){for(let p=this.pendingEffects.length-1;p>=i;p--){const y=this.pendingEffects[p];y.hook.deps=y.previousDeps}this.pendingEffects.length=i}}enqueueUnmountCleanup(i){this.unmountCleanups===void 0&&(this.unmountCleanups=[]),this.unmountCleanups.push(i)}scheduleEffectFlush(i){const p=this.pendingEffects,y=this.unmountCleanups,b=()=>{const E=this.isFlushingEffects;this.isFlushingEffects=!0;try{if(y!==void 0)for(const k of y)(0,r.runCleanup)(k,this.errorReporter);if(p!==void 0)for(const k of p){k.hook.cleanup!==void 0&&((0,r.runCleanup)(k.hook.cleanup,this.errorReporter),k.hook.cleanup=void 0);try{const L=k.effect();k.hook.cleanup=typeof L=="function"?L:void 0}catch(L){this.reportError(L)}}i?.()}finally{this.isFlushingEffects=E}};this.scheduler.scheduleFlush(b)}}t.DILRenderFunctionRuntime=c;function f(D){const i={};if(D!==void 0)for(const p in D)p!=="key"&&(i[p]=D[p]);return i}function g(D,i){if(D===void 0||D.length!==i.length)return!1;for(let p=0;p<D.length;p++){const y=D[p],b=i[p];if(typeof y!="object"||typeof b!="object"){if(y!==b)return!1;continue}if(y.type!==b.type||!w(y.props,b.props)||!g(y.children,b.children))return!1}return!0}function w(D,i){const p=Object.keys(D),y=Object.keys(i);if(p.length!==y.length)return!1;for(const b of p)if(D[b]!==i[b])return!1;return!0}function P(D,i){if((0,n.isDILRuntimeElement)(D)&&D.type!==n.DIL_FRAGMENT){const p=(0,n.readRuntimeElementKey)(D);if(p!==void 0)return\`k:\${typeof p}:\${String(p)}\`}return\`i:\${i}\`}function O(D,i,p){return i===0?p:\`f:\${p}:\${P(D,i)}\`}function V(D){if(D.length===0)return;let i="";for(const p of D)if(!(p==null||typeof p=="boolean")){if(typeof p!="string"&&typeof p!="number")return;i+=String(p)}return i}})(vt,vt.exports)),vt.exports}var jt={exports:{}},Lt={exports:{}},xe;function ln(){return xe||(xe=1,(function(a,t){a.path="dil_renderer/src/DILHtmlViewMessengerTransportRegistry",Object.defineProperty(t,"__esModule",{value:!0}),t.DILHtmlViewMessengerTransportRegistry=void 0;const n=qt();class r{constructor(o){this.errorReporter=o,this.htmlViewMessengerChannelIndex=0,this.htmlViewMessengerTransports=new Map,this.queuedHtmlViewMessengerMessages=new Map}register(o,u){return this.htmlViewMessengerTransports.set(o,u),this.flushQueuedHtmlViewMessengerMessages(o,u),()=>{this.htmlViewMessengerTransports.get(o)===u&&this.htmlViewMessengerTransports.delete(o)}}nextChannelId(){const o=\`dil_html_view_messenger_\${this.htmlViewMessengerChannelIndex}\`;return this.htmlViewMessengerChannelIndex++,o}sendCommand(o,u,d){const s=this.htmlViewMessengerTransports.get(o);if(s===void 0)return new Promise((m,h)=>{this.queueHtmlViewMessengerMessage(o,{data:d,kind:0,reject:h,resolve:m,type:u})});try{return Promise.resolve(s.command(u,d))}catch(m){return Promise.reject(m)}}emitEvent(o,u,d){const s=this.htmlViewMessengerTransports.get(o);if(s===void 0){this.queueHtmlViewMessengerMessage(o,{data:d,kind:1,type:u});return}s.event(u,d)}queueHtmlViewMessengerMessage(o,u){var d;const s=(d=this.queuedHtmlViewMessengerMessages.get(o))!==null&&d!==void 0?d:[];s.push(u),this.queuedHtmlViewMessengerMessages.set(o,s)}flushQueuedHtmlViewMessengerMessages(o,u){const d=this.queuedHtmlViewMessengerMessages.get(o);if(d!==void 0){this.queuedHtmlViewMessengerMessages.delete(o);for(const s of d)if(s.kind===0)try{Promise.resolve(u.command(s.type,s.data)).then(s.resolve,s.reject)}catch(m){s.reject(m)}else try{u.event(s.type,s.data)}catch(m){this.errorReporter.reportError((0,n.toError)(m))}}}}t.DILHtmlViewMessengerTransportRegistry=r})(Lt,Lt.exports)),Lt.exports}var Re;function dn(){return Re||(Re=1,(function(a,t){a.path="dil_renderer/src/DILRendererHtmlViewMessengerState",Object.defineProperty(t,"__esModule",{value:!0}),t.DILRendererHtmlViewMessengerState=void 0;const n=ln();class r{constructor(o){this.receivers=new Map,this.transports=new n.DILHtmlViewMessengerTransportRegistry(o)}registerReceiver(o,u){return this.receivers.set(o,u),()=>{this.receivers.get(o)===u&&this.receivers.delete(o)}}registerTransport(o,u){return this.transports.register(o,u)}receiveCommand(o,u,d){const s=this.receivers.get(o);if(s===void 0)throw new Error(\`Unknown DIL html-view messenger channel "\${o}"\`);return s.command(u,d)}receiveEvent(o,u,d){var s;(s=this.receivers.get(o))===null||s===void 0||s.event(u,d)}nextChannelId(){return this.transports.nextChannelId()}sendCommand(o,u,d){return this.transports.sendCommand(o,u,d)}emitEvent(o,u,d){this.transports.emitEvent(o,u,d)}}t.DILRendererHtmlViewMessengerState=r})(jt,jt.exports)),jt.exports}var Ce;function $t(){return Ce||(Ce=1,(function(a,t){a.path="dil_renderer/src/DILRenderer",Object.defineProperty(t,"__esModule",{value:!0}),t.DILRenderer=t.DILElementRenderNodeKind=t.DILElementChildrenMode=t.DILSkippedChildError=t.DILSkippedChildReason=t.DILRenderErrorMode=t.DILStreamingAnimation=void 0;const n=an(),r=ve(),l=dn();var o;(function(_){_[_.Disabled=0]="Disabled",_[_.Normal=1]="Normal",_[_.Snappy=2]="Snappy",_[_.Neutral=3]="Neutral"})(o||(t.DILStreamingAnimation=o={}));var u;(function(_){_[_.ReportAndAbortRender=0]="ReportAndAbortRender",_[_.ReportAndSkipSubtree=1]="ReportAndSkipSubtree"})(u||(t.DILRenderErrorMode=u={}));var d;(function(_){_.UnregisteredElement="unregistered-element",_.MissingRetainedNode="missing-retained-node",_.NodeIdentityMismatch="node-identity-mismatch"})(d||(t.DILSkippedChildReason=d={}));class s extends Error{constructor(e,c){super(\`Skipped invalid DIL selected child (\${e}).\`),this.reason=e,this.parentNodeType=c,Object.setPrototypeOf(this,s.prototype),this.name="DILSkippedChildError"}}t.DILSkippedChildError=s;var m;(function(_){_[_.RenderedNodes=0]="RenderedNodes",_[_.PrimitiveText=1]="PrimitiveText",_[_.ResolvedValues=2]="ResolvedValues",_[_.OpaqueTree=3]="OpaqueTree"})(m||(t.DILElementChildrenMode=m={}));var h;(function(_){_[_.Element=0]="Element",_[_.Text=1]="Text"})(h||(t.DILElementRenderNodeKind=h={}));class x{constructor(e){this.options=e,this.runtimeHtmlViewMessenger={emitHtmlViewMessengerEvent:(c,f,g)=>{this.emitHtmlViewMessengerEvent(c,f,g)},nextHtmlViewMessengerChannelId:()=>this.nextHtmlViewMessengerChannelId(),registerHtmlViewMessengerReceiver:(c,f)=>this.registerDILHtmlViewMessengerReceiver(c,f),registerHtmlViewMessengerTransport:(c,f)=>this.registerDILHtmlViewMessengerTransport(c,f),sendHtmlViewMessengerCommand:(c,f,g)=>this.sendHtmlViewMessengerCommand(c,f,g)}}createRenderFunction(e){const c=this.createRenderContext(void 0,u.ReportAndAbortRender);return c.updateSource(e),(f,g,w,P)=>{c.render(f,g,w,P),c.didLastRenderFail()||c.commitStateSnapshot()}}createRenderContext(e,c){const f=new n.DILRenderFunctionRuntime(this.options.elementsMetadata,this.options.delegate,this.options.errorReporter,c,this.runtimeHtmlViewMessenger,this.options.scheduler,this.options.trace,(0,r.createDILDateConstructor)(e),e,this.options.genUI,this.options.stateChangeHandler,this.options.speechSynthesis,this.options.dictation,this.options.sourceEvaluatorFactory);return{render:(g,w,P,O)=>{this.options.trace("DILRenderer.render",()=>{var V;f.render(g,(V=O.constants)!==null&&V!==void 0?V:{},w,P)})},commitStateSnapshot:()=>{f.commitStateSnapshot()},didLastRenderFail:()=>f.didLastRenderFail(),setStateSnapshot:(g,w)=>f.setStateSnapshot(g,w),updateSource:g=>{f.evaluate(g)},clearRenderedRoot:()=>{f.clearRenderedRoot()},dispose:()=>{f.dispose()}}}registerDILHtmlViewMessengerTransport(e,c){return this.getHtmlViewMessengerState().registerTransport(e,c)}registerDILHtmlViewMessengerReceiver(e,c){return this.getHtmlViewMessengerState().registerReceiver(e,c)}receiveDILHtmlViewMessengerCommand(e,c,f){const g=this.htmlViewMessengerState;if(g===void 0)throw new Error(\`Unknown DIL html-view messenger channel "\${e}"\`);return g.receiveCommand(e,c,f)}receiveDILHtmlViewMessengerEvent(e,c,f){var g;(g=this.htmlViewMessengerState)===null||g===void 0||g.receiveEvent(e,c,f)}nextHtmlViewMessengerChannelId(){return this.getHtmlViewMessengerState().nextChannelId()}sendHtmlViewMessengerCommand(e,c,f){return this.getHtmlViewMessengerState().sendCommand(e,c,f)}emitHtmlViewMessengerEvent(e,c,f){this.getHtmlViewMessengerState().emitEvent(e,c,f)}getHtmlViewMessengerState(){var e;const c=(e=this.htmlViewMessengerState)!==null&&e!==void 0?e:new l.DILRendererHtmlViewMessengerState(this.options.errorReporter);return this.htmlViewMessengerState=c,c}}t.DILRenderer=x})(yt,yt.exports)),yt.exports}var Ee=$t(),Pt={exports:{}},Kt={exports:{}},we;function un(){return we||(we=1,(function(a){a.path="valdi_core/src/tslib.js";var t,n,r,l,o,u,d,s,m,h,x,_,e,c,f,g,w,P,O,V,D,i,p,y,b,E,k,L,j,z,W;(function(A){var q=typeof oe=="object"?oe:typeof self=="object"?self:typeof this=="object"?this:{};A(F(q,F(a.exports)));function F(B,v){return B!==q&&(typeof Object.create=="function"?Object.defineProperty(B,"__esModule",{value:!0}):B.__esModule=!0),function(I,R){return B[I]=v?v(I,R):R}}})(function(A){var q=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(v,I){v.__proto__=I}||function(v,I){for(var R in I)Object.prototype.hasOwnProperty.call(I,R)&&(v[R]=I[R])};t=function(v,I){if(typeof I!="function"&&I!==null)throw new TypeError("Class extends value "+String(I)+" is not a constructor or null");q(v,I);function R(){this.constructor=v}v.prototype=I===null?Object.create(I):(R.prototype=I.prototype,new R)},n=Object.assign||function(v){for(var I,R=1,C=arguments.length;R<C;R++){I=arguments[R];for(var U in I)Object.prototype.hasOwnProperty.call(I,U)&&(v[U]=I[U])}return v},r=function(v,I){var R={};for(var C in v)Object.prototype.hasOwnProperty.call(v,C)&&I.indexOf(C)<0&&(R[C]=v[C]);if(v!=null&&typeof Object.getOwnPropertySymbols=="function")for(var U=0,C=Object.getOwnPropertySymbols(v);U<C.length;U++)I.indexOf(C[U])<0&&Object.prototype.propertyIsEnumerable.call(v,C[U])&&(R[C[U]]=v[C[U]]);return R},l=function(v,I,R,C){var U=arguments.length,S=U<3?I:C===null?C=Object.getOwnPropertyDescriptor(I,R):C,G;if(typeof Reflect=="object"&&typeof Reflect.decorate=="function")S=Reflect.decorate(v,I,R,C);else for(var M=v.length-1;M>=0;M--)(G=v[M])&&(S=(U<3?G(S):U>3?G(I,R,S):G(I,R))||S);return U>3&&S&&Object.defineProperty(I,R,S),S},o=function(v,I){return function(R,C){I(R,C,v)}},u=function(v,I,R,C,U,S){function G(pt){if(pt!==void 0&&typeof pt!="function")throw new TypeError("Function expected");return pt}for(var M=C.kind,K=M==="getter"?"get":M==="setter"?"set":"value",N=!I&&v?C.static?v:v.prototype:null,T=I||(N?Object.getOwnPropertyDescriptor(N,C.name):{}),$,H=!1,Y=R.length-1;Y>=0;Y--){var et={};for(var nt in C)et[nt]=nt==="access"?{}:C[nt];for(var nt in C.access)et.access[nt]=C.access[nt];et.addInitializer=function(pt){if(H)throw new TypeError("Cannot add initializers after decoration has completed");S.push(G(pt||null))};var it=(0,R[Y])(M==="accessor"?{get:T.get,set:T.set}:T[K],et);if(M==="accessor"){if(it===void 0)continue;if(it===null||typeof it!="object")throw new TypeError("Object expected");($=G(it.get))&&(T.get=$),($=G(it.set))&&(T.set=$),($=G(it.init))&&U.unshift($)}else($=G(it))&&(M==="field"?U.unshift($):T[K]=$)}N&&Object.defineProperty(N,C.name,T),H=!0},d=function(v,I,R){for(var C=arguments.length>2,U=0;U<I.length;U++)R=C?I[U].call(v,R):I[U].call(v);return C?R:void 0},s=function(v){return typeof v=="symbol"?v:"".concat(v)},m=function(v,I,R){return typeof I=="symbol"&&(I=I.description?"[".concat(I.description,"]"):""),Object.defineProperty(v,"name",{configurable:!0,value:R?"".concat(R," ",I):I})},h=function(v,I){if(typeof Reflect=="object"&&typeof Reflect.metadata=="function")return Reflect.metadata(v,I)},x=function(v,I,R,C){function U(S){return S instanceof R?S:new R(function(G){G(S)})}return new(R||(R=Promise))(function(S,G){function M(T){try{N(C.next(T))}catch($){G($)}}function K(T){try{N(C.throw(T))}catch($){G($)}}function N(T){T.done?S(T.value):U(T.value).then(M,K)}N((C=C.apply(v,I||[])).next())})},_=function(v,I){var R={label:0,sent:function(){if(S[0]&1)throw S[1];return S[1]},trys:[],ops:[]},C,U,S,G;return G={next:M(0),throw:M(1),return:M(2)},typeof Symbol=="function"&&(G[Symbol.iterator]=function(){return this}),G;function M(N){return function(T){return K([N,T])}}function K(N){if(C)throw new TypeError("Generator is already executing.");for(;G&&(G=0,N[0]&&(R=0)),R;)try{if(C=1,U&&(S=N[0]&2?U.return:N[0]?U.throw||((S=U.return)&&S.call(U),0):U.next)&&!(S=S.call(U,N[1])).done)return S;switch(U=0,S&&(N=[N[0]&2,S.value]),N[0]){case 0:case 1:S=N;break;case 4:return R.label++,{value:N[1],done:!1};case 5:R.label++,U=N[1],N=[0];continue;case 7:N=R.ops.pop(),R.trys.pop();continue;default:if(S=R.trys,!(S=S.length>0&&S[S.length-1])&&(N[0]===6||N[0]===2)){R=0;continue}if(N[0]===3&&(!S||N[1]>S[0]&&N[1]<S[3])){R.label=N[1];break}if(N[0]===6&&R.label<S[1]){R.label=S[1],S=N;break}if(S&&R.label<S[2]){R.label=S[2],R.ops.push(N);break}S[2]&&R.ops.pop(),R.trys.pop();continue}N=I.call(v,R)}catch(T){N=[6,T],U=0}finally{C=S=0}if(N[0]&5)throw N[1];return{value:N[0]?N[1]:void 0,done:!0}}},e=function(v,I){for(var R in v)R!=="default"&&!Object.prototype.hasOwnProperty.call(I,R)&&j(I,v,R)},j=Object.create?(function(v,I,R,C){C===void 0&&(C=R);var U=Object.getOwnPropertyDescriptor(I,R);(!U||("get"in U?!I.__esModule:U.writable||U.configurable))&&(U={enumerable:!0,get:function(){return I[R]}}),Object.defineProperty(v,C,U)}):(function(v,I,R,C){C===void 0&&(C=R),v[C]=I[R]}),c=function(v){var I=typeof Symbol=="function"&&Symbol.iterator,R=I&&v[I],C=0;if(R)return R.call(v);if(v&&typeof v.length=="number")return{next:function(){return v&&C>=v.length&&(v=void 0),{value:v&&v[C++],done:!v}}};throw new TypeError(I?"Object is not iterable.":"Symbol.iterator is not defined.")},f=function(v,I){var R=typeof Symbol=="function"&&v[Symbol.iterator];if(!R)return v;var C=R.call(v),U,S=[],G;try{for(;(I===void 0||I-- >0)&&!(U=C.next()).done;)S.push(U.value)}catch(M){G={error:M}}finally{try{U&&!U.done&&(R=C.return)&&R.call(C)}finally{if(G)throw G.error}}return S},g=function(){for(var v=[],I=0;I<arguments.length;I++)v=v.concat(f(arguments[I]));return v},w=function(){for(var v=0,I=0,R=arguments.length;I<R;I++)v+=arguments[I].length;for(var C=Array(v),U=0,I=0;I<R;I++)for(var S=arguments[I],G=0,M=S.length;G<M;G++,U++)C[U]=S[G];return C},P=function(v,I,R){if(R||arguments.length===2)for(var C=0,U=I.length,S;C<U;C++)(S||!(C in I))&&(S||(S=Array.prototype.slice.call(I,0,C)),S[C]=I[C]);return v.concat(S||Array.prototype.slice.call(I))},O=function(v){return this instanceof O?(this.v=v,this):new O(v)},V=function(v,I,R){if(!Symbol.asyncIterator)throw new TypeError("Symbol.asyncIterator is not defined.");var C=R.apply(v,I||[]),U,S=[];return U={},G("next"),G("throw"),G("return"),U[Symbol.asyncIterator]=function(){return this},U;function G(H){C[H]&&(U[H]=function(Y){return new Promise(function(et,nt){S.push([H,Y,et,nt])>1||M(H,Y)})})}function M(H,Y){try{K(C[H](Y))}catch(et){$(S[0][3],et)}}function K(H){H.value instanceof O?Promise.resolve(H.value.v).then(N,T):$(S[0][2],H)}function N(H){M("next",H)}function T(H){M("throw",H)}function $(H,Y){H(Y),S.shift(),S.length&&M(S[0][0],S[0][1])}},D=function(v){var I,R;return I={},C("next"),C("throw",function(U){throw U}),C("return"),I[Symbol.iterator]=function(){return this},I;function C(U,S){I[U]=v[U]?function(G){return(R=!R)?{value:O(v[U](G)),done:!1}:S?S(G):G}:S}},i=function(v){if(!Symbol.asyncIterator)throw new TypeError("Symbol.asyncIterator is not defined.");var I=v[Symbol.asyncIterator],R;return I?I.call(v):(v=typeof c=="function"?c(v):v[Symbol.iterator](),R={},C("next"),C("throw"),C("return"),R[Symbol.asyncIterator]=function(){return this},R);function C(S){R[S]=v[S]&&function(G){return new Promise(function(M,K){G=v[S](G),U(M,K,G.done,G.value)})}}function U(S,G,M,K){Promise.resolve(K).then(function(N){S({value:N,done:M})},G)}},p=function(v,I){return Object.defineProperty?Object.defineProperty(v,"raw",{value:I}):v.raw=I,v};var F=Object.create?(function(v,I){Object.defineProperty(v,"default",{enumerable:!0,value:I})}):function(v,I){v.default=I};y=function(v){if(v&&v.__esModule)return v;var I={};if(v!=null)for(var R in v)R!=="default"&&Object.prototype.hasOwnProperty.call(v,R)&&j(I,v,R);return F(I,v),I},b=function(v){return v&&v.__esModule?v:{default:v}},E=function(v,I,R,C){if(R==="a"&&!C)throw new TypeError("Private accessor was defined without a getter");if(typeof I=="function"?v!==I||!C:!I.has(v))throw new TypeError("Cannot read private member from an object whose class did not declare it");return R==="m"?C:R==="a"?C.call(v):C?C.value:I.get(v)},k=function(v,I,R,C,U){if(C==="m")throw new TypeError("Private method is not writable");if(C==="a"&&!U)throw new TypeError("Private accessor was defined without a setter");if(typeof I=="function"?v!==I||!U:!I.has(v))throw new TypeError("Cannot write private member to an object whose class did not declare it");return C==="a"?U.call(v,R):U?U.value=R:I.set(v,R),R},L=function(v,I){if(I===null||typeof I!="object"&&typeof I!="function")throw new TypeError("Cannot use 'in' operator on non-object");return typeof v=="function"?I===v:v.has(I)},z=function(v,I,R){if(I!=null){if(typeof I!="object"&&typeof I!="function")throw new TypeError("Object expected.");var C;if(R){if(!Symbol.asyncDispose)throw new TypeError("Symbol.asyncDispose is not defined.");C=I[Symbol.asyncDispose]}if(C===void 0){if(!Symbol.dispose)throw new TypeError("Symbol.dispose is not defined.");C=I[Symbol.dispose]}if(typeof C!="function")throw new TypeError("Object not disposable.");v.stack.push({value:I,dispose:C,async:R})}else R&&v.stack.push({async:!0});return I};var B=typeof SuppressedError=="function"?SuppressedError:function(v,I,R){var C=new Error(R);return C.name="SuppressedError",C.error=v,C.suppressed=I,C};W=function(v){function I(C){v.error=v.hasError?new B(C,v.error,"An error was suppressed during disposal."):C,v.hasError=!0}function R(){for(;v.stack.length;){var C=v.stack.pop();try{var U=C.dispose&&C.dispose.call(C.value);if(C.async)return Promise.resolve(U).then(R,function(S){return I(S),R()})}catch(S){I(S)}}if(v.hasError)throw v.error}return R()},A("__extends",t),A("__assign",n),A("__rest",r),A("__decorate",l),A("__param",o),A("__esDecorate",u),A("__runInitializers",d),A("__propKey",s),A("__setFunctionName",m),A("__metadata",h),A("__awaiter",x),A("__generator",_),A("__exportStar",e),A("__createBinding",j),A("__values",c),A("__read",f),A("__spread",g),A("__spreadArrays",w),A("__spreadArray",P),A("__await",O),A("__asyncGenerator",V),A("__asyncDelegator",D),A("__asyncValues",i),A("__makeTemplateObject",p),A("__importStar",y),A("__importDefault",b),A("__classPrivateFieldGet",E),A("__classPrivateFieldSet",k),A("__classPrivateFieldIn",L),A("__addDisposableResource",z),A("__disposeResources",W)})})(Kt)),Kt.exports}var At={exports:{}},Ot={exports:{}},De;function cn(){return De||(De=1,(function(a,t){a.path="valdi_core/src/utils/Buffer",Object.defineProperty(t,"__esModule",{value:!0}),t.Buffer=void 0;class n{constructor(l){const o=new ArrayBuffer(l);this.array=new DataView(o),this.pos=0}inner(){return this.array.buffer}empty(){return!this.pos}rewind(){this.pos=0}size(){return this.pos}ensureCapacity(l){const o=this.array;if(l<o.byteLength)return o;const u=new ArrayBuffer(Math.max(o.byteLength*2,l));new Uint8Array(u).set(new Uint8Array(o.buffer));const d=new DataView(u);return this.array=d,d}putUint32(l){const o=this.pos;this.ensureCapacity(o+4).setUint32(o,l,!0),this.pos=o+4}putUint32_2(l,o){const u=this.pos,d=this.ensureCapacity(u+8);d.setUint32(u,l,!0),d.setUint32(u+4,o,!0),this.pos=u+8}putUint32_3(l,o,u){const d=this.pos,s=this.ensureCapacity(d+12);s.setUint32(d,l,!0),s.setUint32(d+4,o,!0),s.setUint32(d+8,u,!0),this.pos=d+12}putUint32_4(l,o,u,d){const s=this.pos,m=this.ensureCapacity(s+16);m.setUint32(s,l,!0),m.setUint32(s+4,o,!0),m.setUint32(s+8,u,!0),m.setUint32(s+12,d,!0),this.pos=s+16}putUint32_5(l,o,u,d,s){const m=this.pos,h=this.ensureCapacity(m+20);h.setUint32(m,l,!0),h.setUint32(m+4,o,!0),h.setUint32(m+8,u,!0),h.setUint32(m+12,d,!0),h.setUint32(m+16,s,!0),this.pos=m+20}putFloat64(l){const o=this.pos;this.ensureCapacity(o+8).setFloat64(o,l,!0),this.pos=o+8}}t.Buffer=n})(Ot,Ot.exports)),Ot.exports}var zt={exports:{}},Se;function pn(){return Se||(Se=1,(function(a,t){a.path="dil_sandbox/src/WeakRef",Object.defineProperty(t,"__esModule",{value:!0}),t.WeakRef=void 0;class n{constructor(o){this.value=o}deref(){return this.value}}const r=globalThis.WeakRef;t.WeakRef=r??n})(zt,zt.exports)),zt.exports}var ke;function _n(){return ke||(ke=1,(function(a,t){a.path="dil_sandbox/src/DILRenderOperations",Object.defineProperty(t,"__esModule",{value:!0}),t.applyDILRenderOperations=t.DILRenderBufferDelegate=void 0;const n=st(),r=Ht(),l=cn(),o=pn();class u{constructor(e){this.elementsMetadata=e,this.attachedStringIndexes=new Map,this.attachedValues=[],this.buffer=new l.Buffer(512),this.functionsById=new Map,this.functionIds=new WeakMap,this.nextFunctionId=1,this.interactionScopes=[]}beginBatch(){this.buffer.rewind(),this.attachedValues=[],this.attachedStringIndexes.clear(),this.interactionScopes=[]}finishBatch(){const e={descriptor:this.buffer.inner(),descriptorSize:this.buffer.size(),attachedValues:this.attachedValues,interactionScopes:this.interactionScopes};return this.buffer.rewind(),this.attachedValues=[],this.attachedStringIndexes.clear(),this.interactionScopes=[],e}discardBatch(){this.buffer.rewind(),this.attachedValues=[],this.attachedStringIndexes.clear(),this.interactionScopes=[]}invokeFunction(e,c){var f;const g=(f=this.functionsById.get(e))===null||f===void 0?void 0:f.deref();if(g===void 0)throw new Error(\`Unknown DIL sandbox function \${e}.\`);return g(...c)}dispose(){this.discardBatch(),this.functionsById.clear()}onElementNodeCreated(e,c){this.writeOperation(1),this.buffer.putUint32_2(e,c)}onNodeInteractionScopeChanged(e,c){this.interactionScopes.push({nodeId:e,scope:c})}onTextNodeCreated(e,c){this.writeOperation(2),this.buffer.putUint32_2(e,this.attach(c))}onNodeDestroyed(e){this.writeOperation(3),this.buffer.putUint32(e)}onNodeMoved(e,c,f){this.writeOperation(4),this.buffer.putUint32_3(e,c,f)}onNodePropChanged(e,c,f){this.writeOperation(5),this.buffer.putUint32_2(e,this.attach(c)),this.writeValue(f)}onNodeOpaqueChildrenChanged(e,c){this.writeOperation(7),this.buffer.putUint32(e),this.writeValue(c)}onTextNodeChanged(e,c){this.writeOperation(6),this.buffer.putUint32_2(e,this.attach(c))}writeOperation(e){this.buffer.putUint32(e)}attach(e){if(typeof e=="string"){const f=this.attachedStringIndexes.get(e);if(f!==void 0)return f;const g=this.attachedValues.length;return this.attachedValues.push(e),this.attachedStringIndexes.set(e,g),g}const c=this.attachedValues.length;return this.attachedValues.push(e),c}writeValue(e){if(e===void 0){this.buffer.putUint32(0);return}if(e===null){this.buffer.putUint32(1);return}if(e===!1){this.buffer.putUint32(2);return}if(e===!0){this.buffer.putUint32(3);return}if(typeof e=="number"){this.buffer.putUint32(4),this.buffer.putFloat64(e);return}if(typeof e=="string"){this.buffer.putUint32_2(5,this.attach(e));return}if(typeof e=="function"){this.buffer.putUint32_2(8,this.getFunctionId(e));return}if(Array.isArray(e)){this.buffer.putUint32_2(6,e.length);for(const c of e)this.writeValue(c);return}if((0,n.isDILRuntimeElement)(e)){this.writeRuntimeElement(e);return}if(m(e)){const c=(0,r.getDILHtmlViewMessengerHost)(e);if(c!==void 0){this.buffer.putUint32_2(12,this.attach(c.channelId));return}const f=Object.keys(e);this.buffer.putUint32_2(7,f.length);for(const g of f)this.buffer.putUint32(this.attach(g)),this.writeValue(e[g]);return}this.buffer.putUint32_2(11,this.attach(e))}writeRuntimeElement(e){const c=e.type;if(c===n.DIL_FRAGMENT||typeof c=="function"){this.buffer.putUint32(10),this.writeValue(e.children);return}const f=this.elementsMetadata[c];if(f===void 0)throw new Error(\`Unknown DIL element type "\${c}".\`);this.buffer.putUint32_2(9,f.typeId),this.writeValue(e.props),this.writeValue(e.children)}getFunctionId(e){let c=this.functionIds.get(e);return c===void 0&&(c=this.nextFunctionId++,this.functionIds.set(e,c)),this.functionsById.set(c,new o.WeakRef(e)),c}}t.DILRenderBufferDelegate=u;function d(_,e,c){var f,g;new s(_,e,c).apply();for(const P of(f=_.interactionScopes)!==null&&f!==void 0?f:[])(g=e.onNodeInteractionScopeChanged)===null||g===void 0||g.call(e,P.nodeId,P.scope)}t.applyDILRenderOperations=d;class s{constructor(e,c,f){this.encoded=e,this.consumer=c,this.valueResolver=f,this.offset=0;const g=e.descriptorSize;if(!Number.isInteger(g)||g<0||g>e.descriptor.byteLength||g%4!==0)throw new Error("Invalid DIL render operations descriptor size.");this.view=new DataView(e.descriptor,0,g)}apply(){for(;this.offset<this.view.byteLength;){const e=this.readUint32();switch(e){case 1:{const c=this.readUint32(),f=this.readUint32();this.consumer.onElementNodeCreated(c,f);break}case 2:{const c=this.readUint32(),f=this.readString();this.consumer.onTextNodeCreated(c,f);break}case 3:this.consumer.onNodeDestroyed(this.readUint32());break;case 4:this.consumer.onNodeMoved(this.readUint32(),this.readUint32(),this.readUint32());break;case 5:this.consumer.onNodePropChanged(this.readUint32(),this.readString(),this.readValue());break;case 6:{const c=this.readUint32(),f=this.readString();this.consumer.onTextNodeChanged(c,f);break}case 7:{const c=this.readUint32(),f=this.readValue();if(!h(f))throw new Error("Invalid DIL render operation opaque children.");this.consumer.onNodeOpaqueChildrenChanged(c,f);break}default:throw new Error(\`Unknown DIL render operation kind \${e}.\`)}}}readValue(){const e=this.readUint32();switch(e){case 0:return;case 1:return null;case 2:return!1;case 3:return!0;case 4:return this.readFloat64();case 5:return this.readString();case 6:{const c=this.readUint32(),f=[];for(let g=0;g<c;g++)f.push(this.readValue());return f}case 7:{const c=this.readUint32(),f={};for(let g=0;g<c;g++)f[this.readString()]=this.readValue();return f}case 8:return this.valueResolver.resolveFunction(this.readUint32());case 9:{const c=this.valueResolver.resolveType(this.readUint32()),f=this.readValue();if(f!==void 0&&!m(f))throw new Error("Invalid DIL render operation element props.");const g=this.readValue();if(!Array.isArray(g))throw new Error("Invalid DIL render operation element children.");return(0,n.createDilElement)(c,f,g)}case 10:{const c=this.readValue();if(!Array.isArray(c))throw new Error("Invalid DIL render operation fragment children.");return(0,n.createDilElement)(n.DIL_FRAGMENT,void 0,c)}case 11:return this.readAttached();case 12:return this.valueResolver.resolveHtmlViewMessenger(this.readString());default:throw new Error(\`Unknown DIL render value kind \${e}.\`)}}readString(){const e=this.readAttached();if(typeof e!="string")throw new Error("Invalid DIL render operation string value.");return e}readAttached(){const e=this.readUint32();if(e>=this.encoded.attachedValues.length)throw new Error("Invalid DIL render operation attached value index.");return this.encoded.attachedValues[e]}readUint32(){if(this.offset+4>this.view.byteLength)throw new Error("Truncated DIL render operations descriptor.");const e=this.view.getUint32(this.offset,!0);return this.offset+=4,e}readFloat64(){if(this.offset+8>this.view.byteLength)throw new Error("Truncated DIL render operations descriptor.");const e=this.view.getFloat64(this.offset,!0);return this.offset+=8,e}}function m(_){if(_===null||typeof _!="object"||(0,n.isDILRuntimeElement)(_))return!1;const e=Object.getPrototypeOf(_);return e===null||e===Object.prototype}function h(_){if(!Array.isArray(_))return!1;for(const e of _)if(typeof e!="string"&&typeof e!="number"&&!x(e))return!1;return!0}function x(_){return m(_)?typeof _.type=="string"&&m(_.props)&&h(_.children):!1}})(At,At.exports)),At.exports}var Ue;function mn(){return Ue||(Ue=1,(function(a,t){a.path="dil_sandbox/src/DILSandboxRuntime",Object.defineProperty(t,"__esModule",{value:!0}),t.DILSandboxRuntime=void 0;const n=un(),r=$t(),l=_n();class o{constructor(s){var{requestRender:m,fixedTimeMs:h,errorMode:x,asyncErrorHandler:_}=s,e=n.__rest(s,["requestRender","fixedTimeMs","errorMode","asyncErrorHandler"]);this.errors=[],this.pendingFlushes=[],this.requestRender=m,this.asyncErrorHandler=_,this.delegate=new l.DILRenderBufferDelegate(e.elementsMetadata),this.renderer=new r.DILRenderer(Object.assign(Object.assign({},e),{delegate:this.delegate,errorReporter:this,scheduler:this})),this.context=this.renderer.createRenderContext(h,x)}reportError(s){this.errors.push(s)}reportUnhandledError(s){this.asyncErrorHandler(s)}scheduleFlush(s){this.pendingFlushes.push(s)}scheduleRender(){this.requestRender()}updateSource(s){return this.errors.length=0,this.context.updateSource(s),this.takeErrors()}setStateSnapshot(s,m){return this.context.setStateSnapshot(s,m)}commitStateSnapshot(){this.context.commitStateSnapshot()}render(s){return{encoded:this.renderOnce(s),errors:this.takeErrors()}}renderSourceUpdate(s){let m=this.renderOnce(s),h=this.takeErrors();return m!==void 0?{encoded:m,errors:h}:(this.context.clearRenderedRoot(),m=this.renderOnce(s),h=u(h,this.takeErrors()),{encoded:m,errors:h})}invokeFunction(s,m){return this.delegate.invokeFunction(s,m)}registerHtmlViewMessengerTransport(s,m){return this.renderer.registerDILHtmlViewMessengerTransport(s,m)}receiveHtmlViewMessengerCommand(s,m,h){return this.renderer.receiveDILHtmlViewMessengerCommand(s,m,h)}receiveHtmlViewMessengerEvent(s,m,h){this.renderer.receiveDILHtmlViewMessengerEvent(s,m,h)}flush(){if(this.pendingFlushes.length===0)return;this.errors.length=0;const s=this.pendingFlushes.splice(0);for(const m of s)m();return this.takeErrors()}dispose(){return this.errors.length=0,this.context.dispose(),this.delegate.dispose(),u(this.takeErrors(),this.flush())}renderOnce(s){if(this.errors.length=0,this.delegate.beginBatch(),this.context.render(s.appData,s.theme,s.activeBreakpoints,{constants:s.constants,isSourceStreaming:!1,streamingAnimation:r.DILStreamingAnimation.Disabled}),this.context.didLastRenderFail()){this.delegate.discardBatch();return}return this.delegate.finishBatch()}takeErrors(){if(this.errors.length!==0)return this.errors.splice(0)}}t.DILSandboxRuntime=o;function u(d,s){return s===void 0?d:d===void 0?s.slice():(d.push(...s),d)}})(Pt,Pt.exports)),Pt.exports}var fn=mn();const hn=globalThis.setInterval.bind(globalThis),je=globalThis.clearInterval.bind(globalThis);class gn{disposed=!1;intervals=new Map;now=Date.now();getSnapshot=()=>this.now;subscribe=(t,n=1e3)=>{if(this.disposed)return()=>{};const r=()=>t();let l=this.intervals.get(n);if(!l){const d=new Set;this.now=Date.now(),l={handle:hn(()=>{this.now=Date.now();for(const m of d)m()},n),listeners:d},this.intervals.set(n,l)}const{handle:o,listeners:u}=l;return u.add(r),()=>{u.delete(r)&&u.size===0&&(je(o),this.intervals.delete(n))}};dispose(){this.disposed=!0;for(const{handle:t,listeners:n}of this.intervals.values())n.clear(),je(t);this.intervals.clear()}}function Le(a,t,n,r){const l=ht.createDilComponentPlaceholder(a,()=>u=>{const d={...u};delete d.__state;const s=d.__resolutionId;if(typeof s=="string"){const h=r().opGenui;let _=Object.getOwnPropertyDescriptor(h?.componentResults??{},s)?.value?.componentName;if(_===void 0)try{const e=JSON.parse(s);Array.isArray(e)&&(_=e[0])}catch{}_!==a&&delete d.__resolutionId}return n.jsx(t,d)}),o=u=>n.jsx(l,u);Object.defineProperty(o,"name",{value:a,configurable:!1});for(const u of Object.keys(t)){const d=Object.getOwnPropertyDescriptor(t,u)?.value;typeof d=="function"&&Object.defineProperty(o,u,{value:Le(\`\${a}.\${u}\`,d,n,r),enumerable:!0})}return o}var rt=Bt(),J=_e();function yn(a){if(a?.kind==="ready")return a.service;if(a?.kind==="requires-auth")return{createSession(t,n){let r=!1;const l=()=>{r||(r=!0,n({kind:J.DILDictationEventKind.Cancelled}))};return{start(){r||(l(),a.requestAuthentication())},stop:l,cancel:l,dispose(){r=!0}}}}}const vn={idle:rt.DILSpeechStatus.Idle,loading:rt.DILSpeechStatus.Loading,playing:rt.DILSpeechStatus.Playing};function In(a,t){const n=yn(a.dictation);if(n)return{isUserInteraction:t,createSession:(r,l)=>n.createSession(r,l)}}const bn={play:rt.DILSpeechFailureStage.Play,request:rt.DILSpeechFailureStage.Request,response:rt.DILSpeechFailureStage.Response,streaming:rt.DILSpeechFailureStage.Streaming};function xn(a,t){const n=a.speechSynthesis;if(n)return{isUserInteraction:t,createSession:(r,l)=>{const o=n.createSession(r,u=>{const d=u.error;l({available:u.available,status:vn[u.status],error:d===null?null:{failureStage:bn[d.failureStage],...d.httpStatus===void 0?{}:{httpStatus:d.httpStatus}}})});return{play:u=>o.play(u),stop:u=>o.stop(u),dispose:()=>o.dispose()}}}}var Gt={exports:{}},Nt={exports:{}},Jt={},Mt={exports:{}},Pe;function Ae(){return Pe||(Pe=1,(function(a,t){a.path="dil_generated_components/src/DILPublicComponentSources.generated",Object.defineProperty(t,"__esModule",{value:!0}),t.DIL_COMPONENT_SOURCES=void 0,t.DIL_COMPONENT_SOURCES=Object.freeze({OpGenuiResolvedComponentBoundary:\`(() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region internal/ResolvedComponentBoundary.dil.tsx
	function ResolvedComponentBoundary({ Component, componentName, componentProps }) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (componentProps.__state !== void 0) return /* @__PURE__ */ __dil.jsx(Component, componentProps);
		const result = (typeof componentProps.__resolutionId === "string" ? opGenui?.componentResults?.[componentProps.__resolutionId] : void 0) ?? opGenui?.componentResults?.[componentDataKey(componentName, componentProps)];
		const delegatesFailedResultToComponent = !opGenui?.showFailedComponents && (componentName === "AsyncImage" || componentName === "AsyncImageGroup");
		if (isComponentResultEnvelope(result) && result.status === "failed" && !delegatesFailedResultToComponent && !((componentName === "Link" || componentName === "LinkCard") && typeof componentProps.title === "string" && componentProps.title.length > 0 && typeof componentProps.url === "string" && componentProps.url.length > 0)) {
			if (componentProps.fallback != null) return componentProps.fallback;
			return opGenui?.showFailedComponents ? /* @__PURE__ */ __dil.jsx("text", {
				color: "danger",
				size: "sm"
			}, \\\`\\\${componentName}: \\\${result.code}\\\`) : null;
		}
		return /* @__PURE__ */ __dil.jsx(Component, componentProps);
	}

//#endregion
return ResolvedComponentBoundary;
})();
return __dilDefaultExport;
})()\`,AsyncImage:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentAsyncImage(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "AsyncImage",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "AsyncImage", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentAsyncImage, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function hasImageSource(image) {
		return Boolean(image?.content_url || image?.thumbnail_url);
	}
	function normalizedAspectRatio(value) {
		return value?.replace(":", " / ");
	}
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region AsyncImage.dil.tsx
/** Mirror v1: only pixel max-widths supply a non-shrinking preferred width. */
	function hasPreferredPixelWidth(value) {
		if (typeof value === "number") return Number.isFinite(value) && value >= 0;
		return typeof value === "string" && /^\\\\d+(?:\\\\.\\\\d+)?(?:px)?$/.test(value.trim());
	}
	function normalizedDimension(value) {
		return typeof value === "string" && /^\\\\d+(?:\\\\.\\\\d+)?$/.test(value.trim()) ? Number(value) : value;
	}
	/** Mirror the v1 intrinsic frame while image resolution is still pending. */
	function unresolvedAspectWidth(maxHeight, aspectRatio) {
		return \\\`calc(\\\${typeof maxHeight === "number" ? \\\`\\\${maxHeight}px\\\` : /^\\\\d+(?:\\\\.\\\\d+)?$/.test(maxHeight.trim()) ? \\\`\\\${maxHeight.trim()}px\\\` : maxHeight.trim()} * \\\${normalizedAspectRatio(aspectRatio)})\\\`;
	}
	function containsSource(value, src) {
		if (value === src) return true;
		if (value === null || typeof value !== "object") return false;
		return Object.values(value).some((child) => containsSource(child, src));
	}
	function directImages(props, sourceAllowed) {
		if (!sourceAllowed || !props.src || props.query != null || props.ref != null) return [];
		return [{
			url: props.src,
			content_url: props.src,
			thumbnail_url: props.src,
			title: props.alt ?? ""
		}];
	}
	function ImageContent(props) {
		return /* @__PURE__ */ __dil.jsx("search-image", props);
	}
	function AsyncImage(props) {
		const sourceAllowed = DIL.useAppData((appData) => Boolean(props.src) && containsSource(appData.opGenui?.modelDataBindings, props.src));
		const resolved = useResolvedComponentResult("AsyncImage", props);
		const isDirect = "src" in props;
		const state = isDirect ? void 0 : resolved.state;
		const resolutionComplete = isDirect || resolved.resolutionComplete;
		const isLoading = state?.is_loading ?? !resolutionComplete;
		const images = isDirect ? directImages(props, sourceAllowed) : state?.images ?? [];
		const image = images[0];
		const hasImage = hasImageSource(image);
		const isProduct = image?.refs?.some((ref) => ref.ref_type === "product");
		const needsLocalSizing = state == null;
		const frameMaxWidth = state?.frame_max_width ?? normalizedDimension(props.maxWidth) ?? (needsLocalSizing ? "100%" : void 0);
		const frameAspectRatio = state?.frame_aspect_ratio ?? props.aspectRatio;
		const presentation = state?.presentation;
		const hasAuthoredDimensions = props.width != null || props.height != null;
		const defaultMaxHeight = needsLocalSizing && !hasAuthoredDimensions ? normalizedDimension(props.maxHeight) ?? 220 : void 0;
		const unresolvedSize = frameAspectRatio == null ? defaultMaxHeight : void 0;
		const unresolvedWidth = defaultMaxHeight != null && frameAspectRatio != null ? unresolvedAspectWidth(defaultMaxHeight, frameAspectRatio) : unresolvedSize;
		const fallbackElement = props.fallback ?? /* @__PURE__ */ __dil.jsx("box", {
			align: "center",
			ariaLabel: "Image unavailable",
			background: "surface-tertiary",
			height: "100%",
			justify: "center",
			role: "img",
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("icon", {
			color: "tertiary",
			name: "image-square",
			size: "lg"
		}));
		const imageElement = /* @__PURE__ */ __dil.jsx(ImageContent, {
			key: isDirect ? props.src : void 0,
			attributionOverlay: !isDirect,
			alt: props.alt ?? image?.title ?? "",
			aspectRatio: normalizedAspectRatio(frameAspectRatio),
			background: isProduct ? "#f3f3f3" : void 0,
			bitmapBlendMode: isProduct ? "darken" : void 0,
			containOutsideAspectRatio: isProduct && props.fit == null ? {
				min: .6,
				max: 1.8
			} : void 0,
			fallback: fallbackElement,
			contentCropFit: props.fit,
			fit: props.fit ?? (isProduct ? "cover" : state?.image_fit ?? (isDirect && frameAspectRatio == null ? "contain" : "cover")),
			height: "100%",
			hideOnFailure: true,
			minHeight: isProduct ? 0 : void 0,
			position: isProduct ? "center" : void 0,
			radius: "none",
			searchImage: image,
			width: "100%"
		});
		return /* @__PURE__ */ __dil.jsx("box", {
			imageObservation: !isLoading && !hasImage ? "unavailable" : "active",
			ariaLabel: isLoading ? "Loading image" : void 0,
			aspectRatio: normalizedAspectRatio(frameAspectRatio),
			background: "surface-tertiary",
			border: (presentation?.frame ?? props.frame ?? true) && !isLoading && hasImage ? {
				color: "subtle",
				size: 1
			} : void 0,
			clip: true,
			flex: hasPreferredPixelWidth(frameMaxWidth) ? "0 0 auto" : void 0,
			height: state?.frame_height ?? normalizedDimension(props.height) ?? (props.width != null || frameAspectRatio != null ? void 0 : unresolvedSize ?? "100%"),
			maxHeight: (state?.max_height !== void 0 ? state.max_height ?? void 0 : normalizedDimension(props.maxHeight) ?? defaultMaxHeight) ?? (isProduct ? "100%" : void 0),
			maxWidth: frameMaxWidth,
			minHeight: presentation?.min_height ?? props.minHeight,
			minWidth: presentation?.min_width ?? props.minWidth ?? 0,
			radius: presentation?.radius ?? props.radius ?? "2xl",
			role: isLoading ? "status" : void 0,
			width: state?.frame_width ?? normalizedDimension(props.width) ?? unresolvedWidth ?? "100%"
		}, !isLoading && !hasImage ? fallbackElement : /* @__PURE__ */ __dil.jsx("pressable", {
			ariaLabel: image?.title ? \\\`Open \\\${image.title}\\\` : "Open image",
			background: "transparent",
			disabled: isLoading,
			height: "100%",
			onClick: () => state?.default_on_click_action ? GenUI.dispatchAction(state.default_on_click_action) : GenUI.openImageLightbox(images, 0),
			radius: "none",
			width: "100%"
		}, imageElement));
	}

//#endregion
return AsyncImage;
})();
return __dilDefaultExport;
})()))\`,AsyncImageGroup:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentAsyncImageGroup(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "AsyncImageGroup",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "AsyncImageGroup", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentAsyncImageGroup, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function hasImageSource(image) {
		return Boolean(image?.content_url || image?.thumbnail_url);
	}
	function normalizedAspectRatio(value) {
		return value?.replace(":", " / ");
	}
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region AsyncImageGroup.dil.tsx
/** Display the complete gallery count on the final visible desktop tile. */
	function ImageCount({ count }) {
		return /* @__PURE__ */ __dil.jsx("col", {
			align: "end",
			ariaHidden: true,
			height: "100%",
			justify: "end",
			padding: {
				bottom: 2.5,
				right: 2.5
			},
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			background: "rgba(0, 0, 0, 0.4)",
			gap: .5,
			padding: {
				bottom: 1,
				left: 1.5,
				right: 2,
				top: 1
			},
			radius: "full",
			width: "fit-content"
		}, /* @__PURE__ */ __dil.jsx("icon", {
			color: "white",
			name: "images",
			size: "xs"
		}), /* @__PURE__ */ __dil.jsx("text", {
			color: "white",
			inline: true,
			size: "xs",
			weight: "semibold"
		}, count)));
	}
	const PLACEHOLDER_INDEXES = [
		0,
		1,
		2
	];
	function ImageFailure() {
		return /* @__PURE__ */ __dil.jsx("box", {
			align: "center",
			ariaLabel: "Image unavailable",
			background: "surface-tertiary",
			height: "100%",
			justify: "center",
			role: "img",
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("icon", {
			color: "tertiary",
			name: "image-square",
			size: "lg"
		}));
	}
	function ImageFallbackFrame({ aspectRatio, children, itemHeight, itemWidth }) {
		return /* @__PURE__ */ __dil.jsx("box", {
			imageObservation: "unavailable",
			aspectRatio: normalizedAspectRatio(aspectRatio ?? "5:4"),
			clip: true,
			height: itemHeight,
			minWidth: itemWidth,
			radius: "2xl",
			width: itemWidth
		}, children);
	}
	/** Keep one loading surface mounted while its resolved image source arrives. */
	function GroupImageTile({ aspectRatio, failed = false, fallback, flex, height, image, images, index, minWidth, radius, showCount = false, width }) {
		const isProduct = image?.refs?.some((ref) => ref.ref_type === "product");
		return /* @__PURE__ */ __dil.jsx("box", {
			imageObservation: failed ? "unavailable" : "active",
			aspectRatio: normalizedAspectRatio(aspectRatio),
			background: "surface-tertiary",
			clip: true,
			flex,
			height,
			minWidth,
			radius,
			width
		}, failed ? /* @__PURE__ */ __dil.jsx(ImageFailure, null) : /* @__PURE__ */ __dil.jsx("search-image", {
			alt: image?.title ?? "",
			aspectRatio: normalizedAspectRatio(aspectRatio),
			background: isProduct ? "#f3f3f3" : void 0,
			bitmapBlendMode: isProduct ? "darken" : void 0,
			containOutsideAspectRatio: isProduct ? {
				min: .6,
				max: 1.8
			} : void 0,
			fallback,
			fit: "cover",
			frame: true,
			height: "100%",
			hideOnFailure: true,
			minHeight: isProduct ? 0 : void 0,
			position: isProduct ? "center" : void 0,
			radius,
			searchImage: image,
			width: "100%"
		}, image ? /* @__PURE__ */ __dil.jsx("pressable", {
			ariaLabel: image.title ? \\\`Open \\\${image.title}\\\` : "Open image",
			background: "transparent",
			height: "100%",
			onClick: () => GenUI.openImageLightbox(images, index),
			radius: "none",
			width: "100%"
		}, showCount ? /* @__PURE__ */ __dil.jsx(ImageCount, { count: images.length }) : null) : null));
	}
	/** Render the v1-compatible responsive image strip, preserving loading and fallback states. */
	function AsyncImageGroup(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("AsyncImageGroup", props);
		const isSm = DIL.useBreakpoint("sm");
		const layout = state?.layout ?? props.layout ?? "carousel";
		const isDesktopCarousel = layout === "carousel" && isSm;
		const images = (state?.images ?? []).filter(hasImageSource);
		const isLoading = state?.is_loading ?? !resolutionComplete;
		const failed = !isLoading && images.length === 0;
		const aspectRatio = state?.aspect_ratio ?? props.aspectRatio ?? "5:4";
		const itemHeight = state?.item_height;
		const itemWidth = state?.item_width;
		if (failed && props.fallback) return /* @__PURE__ */ __dil.jsx(ImageFallbackFrame, {
			aspectRatio,
			itemHeight,
			itemWidth
		}, props.fallback);
		const fallbackElement = props.fallback ?? /* @__PURE__ */ __dil.jsx(ImageFailure, null);
		if (layout === "bento" && (isLoading || failed || images.length >= 3)) return /* @__PURE__ */ __dil.jsx("row", {
			ariaLabel: isLoading ? "Loading images" : void 0,
			aspectRatio: "363 / 200",
			flexInRow: true,
			gap: .5,
			maxWidth: 768,
			minHeight: 0,
			minWidth: 0,
			role: isLoading ? "status" : void 0,
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx(GroupImageTile, {
			failed,
			fallback: fallbackElement,
			flex: 1,
			height: "100%",
			image: images[0],
			images,
			index: 0,
			radius: {
				bottomLeft: "2xl",
				topLeft: "2xl"
			},
			width: "100%"
		}), /* @__PURE__ */ __dil.jsx("col", {
			gap: .5,
			width: "40%"
		}, [1, 2].map((index) => /* @__PURE__ */ __dil.jsx(GroupImageTile, {
			failed,
			fallback: fallbackElement,
			flex: 1,
			height: "100%",
			image: images[index],
			images,
			index,
			key: \\\`async-image-group-tile:\\\${index}\\\`,
			radius: index === 1 ? { topRight: "2xl" } : { bottomRight: "2xl" },
			showCount: index === 2 && images.length > 3,
			width: "100%"
		}))));
		if (isDesktopCarousel && (isLoading || failed || images.length >= 3)) return /* @__PURE__ */ __dil.jsx("row", {
			flexInRow: true,
			maxWidth: 768,
			minWidth: 0,
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("row", {
			ariaLabel: isLoading ? "Loading images" : void 0,
			gap: 2,
			maxWidth: "100%",
			minWidth: 0,
			role: isLoading ? "status" : void 0,
			width: 768
		}, PLACEHOLDER_INDEXES.map((index) => /* @__PURE__ */ __dil.jsx(GroupImageTile, {
			aspectRatio,
			failed,
			fallback: fallbackElement,
			flex: "1 1 0%",
			image: images[index],
			images,
			index,
			key: \\\`async-image-group-tile:\\\${index}\\\`,
			minWidth: 0,
			radius: "2xl",
			showCount: index === 2 && images.length > 3,
			width: "100%"
		}))));
		const scrollImages = isLoading || failed ? PLACEHOLDER_INDEXES.map(() => void 0) : images;
		return /* @__PURE__ */ __dil.jsx("box", {
			ariaLabel: isLoading ? "Loading images" : void 0,
			flexInRow: true,
			role: isLoading ? "status" : void 0,
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("carousel", {
			flush: true,
			gap: 2,
			showArrows: false,
			snap: "none"
		}, scrollImages.map((image, index) => /* @__PURE__ */ __dil.jsx("carousel-item", {
			height: itemHeight,
			key: \\\`async-image-group-tile:\\\${index}\\\`,
			minWidth: itemWidth,
			width: itemWidth ?? (isLoading || failed ? 140 : void 0)
		}, /* @__PURE__ */ __dil.jsx(GroupImageTile, {
			aspectRatio,
			failed,
			fallback: fallbackElement,
			height: itemHeight,
			image,
			images,
			index,
			radius: "2xl",
			width: "100%"
		})))));
	}

//#endregion
return AsyncImageGroup;
})();
return __dilDefaultExport;
})()))\`,AsyncVideo:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentAsyncVideo(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "AsyncVideo",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "AsyncVideo", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentAsyncVideo, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region AsyncVideo.dil.tsx
	function AsyncVideo(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("AsyncVideo", props);
		if (!state) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("loading", { label: "Loading video" });
		const video = state.video;
		if (!video) return null;
		return /* @__PURE__ */ __dil.jsx("box", {
			background: "surface-secondary",
			clip: true,
			maxWidth: "100%",
			radius: "3xl",
			width: "640px"
		}, /* @__PURE__ */ __dil.jsx("youtube", {
			aspectRatio: "16 / 9",
			maxWidth: "100%",
			title: video.title,
			videoId: video.video_id,
			width: "100%"
		}));
	}

//#endregion
return AsyncVideo;
})();
return __dilDefaultExport;
})()))\`,AutomationPlanSummary:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentAutomationPlanSummary(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "AutomationPlanSummary",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "AutomationPlanSummary", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentAutomationPlanSummary, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) {
			if (result.status === "pending") return {
				status: "pending",
				resolutionComplete: false,
				state: void 0
			};
			return result.status === "resolved" ? {
				status: "resolved",
				resolutionComplete: true,
				state: result.state
			} : {
				status: "failed",
				resolutionComplete: true,
				state: void 0
			};
		}
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "pending") return true;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region AutomationPlanSummary.dil.tsx
	function AutomationPlanSummary(props) {
		const [expanded, setExpanded] = DIL.useState(false);
		const [collapsed, setCollapsed] = DIL.useState(false);
		const { state, resolutionComplete } = useResolvedComponentResult("AutomationPlanSummary", props);
		if (!state) return resolutionComplete ? props.fallback ?? null : /* @__PURE__ */ __dil.jsx("loading-block", {
			width: "100%",
			height: 240
		});
		const currentStepIndex = state.steps?.findIndex((step) => step.status === "in_progress") ?? -1;
		const nextStepIndex = currentStepIndex >= 0 ? currentStepIndex : state.steps?.findIndex((step) => step.status === "pending") ?? -1;
		const previousStepCompleted = nextStepIndex > 0 && state.steps?.[nextStepIndex - 1]?.status === "completed";
		const firstVisibleIndex = expanded ? 0 : nextStepIndex < 0 ? Math.max(0, (state.steps?.length ?? 0) - 3) : Math.max(0, nextStepIndex - (previousStepCompleted ? 1 : 0));
		const visibleSteps = expanded ? state.steps : state.steps?.slice(firstVisibleIndex, firstVisibleIndex + 3);
		return /* @__PURE__ */ __dil.jsx("card", {
			size: "full",
			padding: 5,
			gap: 4
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "start",
			gap: 2
		}, /* @__PURE__ */ __dil.jsx("col", {
			flex: 1,
			minWidth: 0,
			gap: 1
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "sm",
			color: "tertiary"
		}, state.goal_label), /* @__PURE__ */ __dil.jsx("title", { size: "sm" }, state.title)), /* @__PURE__ */ __dil.jsx("row", {
			align: "start",
			gap: 2,
			flex: "0 0 auto"
		}, state.show_plan_label && state.hide_plan_label && /* @__PURE__ */ __dil.jsx("pressable", {
			size: 16,
			align: "center",
			justify: "center",
			radius: "full",
			ariaLabel: collapsed ? state.show_plan_label : state.hide_plan_label,
			tooltip: collapsed ? state.show_plan_label : state.hide_plan_label,
			onClick: () => setCollapsed(!collapsed)
		}, /* @__PURE__ */ __dil.jsx("icon", {
			name: collapsed ? "expand-large" : "collapse-large",
			size: "md",
			color: "tertiary"
		})), state.steps != null && state.expand_label && state.collapse_label && /* @__PURE__ */ __dil.jsx("pressable", {
			size: 16,
			align: "center",
			justify: "center",
			radius: "full",
			ariaLabel: !collapsed && expanded ? state.collapse_label : state.expand_label,
			tooltip: !collapsed && expanded ? state.collapse_label : state.expand_label,
			onClick: () => {
				setExpanded(collapsed || !expanded);
				setCollapsed(false);
			}
		}, /* @__PURE__ */ __dil.jsx("icon", {
			name: "dots-horizontal",
			size: "md",
			color: "tertiary"
		})))), !collapsed && /* @__PURE__ */ __dil.jsx("box", {
			height: 1,
			background: {
				light: "#ededed",
				dark: "alpha-10"
			},
			ariaHidden: true
		}), !collapsed && (state.steps == null ? /* @__PURE__ */ __dil.jsx("col", {
			gap: 3,
			padding: { y: 5 }
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "md",
			color: "secondary"
		}, state.summary_error_label)) : /* @__PURE__ */ __dil.jsx("col", {
			gap: 0,
			role: "list"
		}, visibleSteps?.map((step, index) => /* @__PURE__ */ __dil.jsx("row", {
			key: step.id,
			gap: 1,
			align: "stretch",
			minHeight: index === visibleSteps.length - 1 ? 20 : 50,
			role: "listitem"
		}, /* @__PURE__ */ __dil.jsx("col", {
			width: 28,
			flex: "0 0 auto",
			align: "center",
			gap: 0
		}, /* @__PURE__ */ __dil.jsx("box", {
			height: step.status === "in_progress" ? 28 : 20,
			flex: "0 0 auto",
			align: "center",
			justify: "center",
			role: "img",
			ariaLabel: state.status_labels[step.status]
		}, /* @__PURE__ */ __dil.jsx("icon", {
			name: step.status === "completed" ? "check-circle-filled" : step.status === "in_progress" ? "spinner" : "empty-circle",
			size: "xl",
			color: step.status === "pending" ? {
				light: "gray-300",
				dark: "gray-600"
			} : step.status === "in_progress" ? "secondary" : "tertiary"
		})), index < visibleSteps.length - 1 && /* @__PURE__ */ __dil.jsx("box", {
			width: 1,
			flex: 1,
			background: {
				light: "#ededed",
				dark: "alpha-10"
			},
			ariaHidden: true
		})), /* @__PURE__ */ __dil.jsx("col", {
			gap: 1,
			flex: 1,
			minWidth: 0,
			padding: { bottom: index === visibleSteps.length - 1 ? 0 : 2 }
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "md",
			color: step.status === "completed" ? "tertiary" : "primary"
		}, step.title), expanded && step.description && /* @__PURE__ */ __dil.jsx("text", {
			size: "sm",
			color: "secondary"
		}, step.description)))))));
	}

//#endregion
return AutomationPlanSummary;
})();
return __dilDefaultExport;
})()))\`,Citation:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentCitation(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "Citation",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "Citation", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentCitation, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) {
			if (result.status === "pending") return {
				status: "pending",
				resolutionComplete: false,
				state: void 0
			};
			return result.status === "resolved" ? {
				status: "resolved",
				resolutionComplete: true,
				state: result.state
			} : {
				status: "failed",
				resolutionComplete: true,
				state: void 0
			};
		}
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "pending") return true;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region Cite.dil.tsx
	function clampedIndex(index, itemCount) {
		if (typeof index === "number" && Number.isInteger(index) && index >= 0 && index < itemCount) return index;
		return 0;
	}
	function ResolvedCite({ activeIndex: serverActiveIndex, items }) {
		const initialActiveIndex = clampedIndex(serverActiveIndex, items.length);
		const [activeIndex, setActiveIndex] = DIL.useState(initialActiveIndex);
		const [badgeHovered, setBadgeHovered] = DIL.useState(false);
		const isSm = DIL.useBreakpoint("sm");
		const isMobile = DIL.useIsMobile();
		DIL.useEffect(() => {
			setActiveIndex(initialActiveIndex);
		}, [serverActiveIndex, items.length]);
		const activeItem = items[clampedIndex(activeIndex, items.length)];
		const url = activeItem.url ?? "";
		const hasUrl = url !== "";
		const hasDetails = [
			activeItem.title,
			activeItem.metadata_label,
			activeItem.snippet,
			activeItem.thumbnail_url
		].some(Boolean);
		const showPopover = items.length > 1 || (!isMobile || !hasUrl) && hasDetails;
		const isInteractive = showPopover || hasUrl;
		const sourceLabel = activeItem.source_label ?? activeItem.title ?? "Source";
		const badge = /* @__PURE__ */ __dil.jsx("badge", {
			color: isInteractive && badgeHovered ? {
				light: "#e8e8e8",
				dark: "gray-300"
			} : {
				light: "gray-75",
				dark: "gray-400"
			},
			maxWidth: "100%",
			onHover: isInteractive ? setBadgeHovered : void 0,
			padding: {
				left: 1,
				right: 1.5,
				y: 1
			},
			size: "sm",
			variant: "soft",
			weight: "normal"
		}, hasUrl ? /* @__PURE__ */ __dil.jsx("row", { flex: "none" }, /* @__PURE__ */ __dil.jsx("favicon", {
			size: 12,
			url
		})) : null, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			flex: "0 1 auto",
			gap: .75,
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("row", {
			flex: "0 1 auto",
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			inline: true,
			maxLines: 1,
			size: "3xs",
			truncate: true
		}, sourceLabel)), items.length > 1 ? /* @__PURE__ */ __dil.jsx("row", { flex: "none" }, /* @__PURE__ */ __dil.jsx("text", {
			color: "tertiary",
			inline: true,
			size: "3xs"
		}, "+", items.length - 1)) : null));
		if (!showPopover) return hasUrl ? /* @__PURE__ */ __dil.jsx("pressable", {
			ariaLabel: \\\`Open \\\${sourceLabel}\\\`,
			inline: true,
			onClick: () => GenUI.openUrl(url)
		}, badge) : badge;
		return /* @__PURE__ */ __dil.jsx("popover", {
			hoverOpenDelay: 80,
			showOnHover: true
		}, /* @__PURE__ */ __dil.jsx("popover-trigger", { onClick: () => !isMobile && isSm && hasUrl ? GenUI.openUrl(url) : void 0 }, badge), /* @__PURE__ */ __dil.jsx("popover-content", {
			align: "start",
			showCloseButton: isMobile ? false : void 0,
			side: "bottom",
			sideOffset: 8
		}, isMobile ? /* @__PURE__ */ __dil.jsx(MobileCitationSources, { items }) : /* @__PURE__ */ __dil.jsx(DesktopCitationSources, { items })));
	}
	function getCitationDetail(item) {
		return [item.metadata_label, item.snippet].filter(Boolean).join(" — ");
	}
	function DesktopCitationSources({ items }) {
		return /* @__PURE__ */ __dil.jsx("col", {
			gap: 0,
			maxHeight: 320,
			maxWidth: "100%",
			padding: {
				bottom: 2,
				top: 2,
				x: 5
			},
			scrollable: true,
			width: 384
		}, items.map((item, index) => {
			const itemUrl = item.url ?? "";
			const itemSourceLabel = item.source_label ?? item.title ?? "Source";
			const itemTitle = item.title ?? "";
			const itemDetail = getCitationDetail(item);
			const source = /* @__PURE__ */ __dil.jsx("col", {
				border: index === 0 ? void 0 : { top: {
					color: "default",
					size: 1
				} },
				gap: 1.5,
				padding: { y: 3 },
				width: "100%"
			}, /* @__PURE__ */ __dil.jsx("row", {
				align: "center",
				gap: 2,
				minWidth: 0,
				width: "100%"
			}, itemUrl ? /* @__PURE__ */ __dil.jsx("favicon", {
				size: 16,
				url: itemUrl
			}) : null, /* @__PURE__ */ __dil.jsx("text", {
				color: "secondary",
				inline: true,
				maxLines: 1,
				size: "xs",
				truncate: true
			}, itemSourceLabel)), itemTitle ? /* @__PURE__ */ __dil.jsx("text", {
				maxLines: 2,
				size: "sm",
				weight: "medium"
			}, itemTitle) : null, itemDetail || item.thumbnail_url ? /* @__PURE__ */ __dil.jsx("row", {
				align: "start",
				gap: 4,
				width: "100%"
			}, /* @__PURE__ */ __dil.jsx("col", {
				flex: 1,
				minWidth: 0
			}, itemDetail ? /* @__PURE__ */ __dil.jsx("text", {
				color: "secondary",
				maxLines: 3,
				size: "sm"
			}, itemDetail) : null), item.thumbnail_url ? /* @__PURE__ */ __dil.jsx("image", {
				alt: "",
				fit: "cover",
				height: 56,
				hideOnFailure: true,
				radius: "xl",
				src: item.thumbnail_url,
				width: 56
			}) : null) : null);
			return itemUrl ? /* @__PURE__ */ __dil.jsx("pressable", {
				ariaLabel: \\\`Open \\\${itemSourceLabel}\\\`,
				key: \\\`\\\${itemUrl}:\\\${index}\\\`,
				onClick: () => GenUI.openUrl(itemUrl),
				width: "100%"
			}, source) : /* @__PURE__ */ __dil.jsx("col", {
				key: \\\`source:\\\${index}\\\`,
				width: "100%"
			}, source);
		}));
	}
	function MobileCitationSources({ items }) {
		return /* @__PURE__ */ __dil.jsx("col", {
			gap: 3,
			maxHeight: 560,
			scrollable: true,
			width: "100%"
		}, items.map((item, index) => {
			const itemUrl = item.url ?? "";
			const itemSourceLabel = item.source_label ?? item.title ?? "Source";
			const itemTitle = item.title ?? "";
			const source = /* @__PURE__ */ __dil.jsx("col", {
				gap: 1,
				width: "100%"
			}, /* @__PURE__ */ __dil.jsx("row", {
				align: "center",
				gap: 2,
				minWidth: 0,
				width: "100%"
			}, itemUrl ? /* @__PURE__ */ __dil.jsx("favicon", {
				size: 16,
				url: itemUrl
			}) : null, /* @__PURE__ */ __dil.jsx("text", {
				size: "sm",
				weight: "semibold"
			}, itemSourceLabel)), itemTitle ? /* @__PURE__ */ __dil.jsx("text", { size: "sm" }, itemTitle) : null, item.metadata_label ? /* @__PURE__ */ __dil.jsx("caption", null, item.metadata_label) : null);
			return /* @__PURE__ */ __dil.jsx("col", {
				gap: 3,
				key: \\\`\\\${itemUrl}:\\\${index}\\\`,
				width: "100%"
			}, index > 0 ? /* @__PURE__ */ __dil.jsx("divider", { color: "subtle" }) : null, itemUrl ? /* @__PURE__ */ __dil.jsx("pressable", {
				ariaLabel: \\\`Open \\\${itemSourceLabel}\\\`,
				onClick: () => GenUI.openUrl(itemUrl),
				width: "100%"
			}, source) : source);
		}));
	}
	function Cite(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("Cite", props);
		if (!state) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("text", { inline: true }, /* @__PURE__ */ __dil.jsx("loading", { label: "Loading citations" }));
		const items = state.items ?? [];
		if (!items.length) return null;
		return /* @__PURE__ */ __dil.jsx("text", { inline: true }, /* @__PURE__ */ __dil.jsx(ResolvedCite, {
			activeIndex: state.active_index,
			items
		}));
	}

//#endregion
//#region Citation.dil.tsx
	function Citation(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("Citation", props);
		if (!state) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("text", { inline: true }, /* @__PURE__ */ __dil.jsx("loading", { label: "Loading citation" }));
		return /* @__PURE__ */ __dil.jsx(Cite, {
			...props,
			__state: state
		});
	}

//#endregion
return Citation;
})();
return __dilDefaultExport;
})()))\`,Cite:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentCite(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "Cite",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "Cite", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentCite, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) {
			if (result.status === "pending") return {
				status: "pending",
				resolutionComplete: false,
				state: void 0
			};
			return result.status === "resolved" ? {
				status: "resolved",
				resolutionComplete: true,
				state: result.state
			} : {
				status: "failed",
				resolutionComplete: true,
				state: void 0
			};
		}
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "pending") return true;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region Cite.dil.tsx
	function clampedIndex(index, itemCount) {
		if (typeof index === "number" && Number.isInteger(index) && index >= 0 && index < itemCount) return index;
		return 0;
	}
	function ResolvedCite({ activeIndex: serverActiveIndex, items }) {
		const initialActiveIndex = clampedIndex(serverActiveIndex, items.length);
		const [activeIndex, setActiveIndex] = DIL.useState(initialActiveIndex);
		const [badgeHovered, setBadgeHovered] = DIL.useState(false);
		const isSm = DIL.useBreakpoint("sm");
		const isMobile = DIL.useIsMobile();
		DIL.useEffect(() => {
			setActiveIndex(initialActiveIndex);
		}, [serverActiveIndex, items.length]);
		const activeItem = items[clampedIndex(activeIndex, items.length)];
		const url = activeItem.url ?? "";
		const hasUrl = url !== "";
		const hasDetails = [
			activeItem.title,
			activeItem.metadata_label,
			activeItem.snippet,
			activeItem.thumbnail_url
		].some(Boolean);
		const showPopover = items.length > 1 || (!isMobile || !hasUrl) && hasDetails;
		const isInteractive = showPopover || hasUrl;
		const sourceLabel = activeItem.source_label ?? activeItem.title ?? "Source";
		const badge = /* @__PURE__ */ __dil.jsx("badge", {
			color: isInteractive && badgeHovered ? {
				light: "#e8e8e8",
				dark: "gray-300"
			} : {
				light: "gray-75",
				dark: "gray-400"
			},
			maxWidth: "100%",
			onHover: isInteractive ? setBadgeHovered : void 0,
			padding: {
				left: 1,
				right: 1.5,
				y: 1
			},
			size: "sm",
			variant: "soft",
			weight: "normal"
		}, hasUrl ? /* @__PURE__ */ __dil.jsx("row", { flex: "none" }, /* @__PURE__ */ __dil.jsx("favicon", {
			size: 12,
			url
		})) : null, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			flex: "0 1 auto",
			gap: .75,
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("row", {
			flex: "0 1 auto",
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			inline: true,
			maxLines: 1,
			size: "3xs",
			truncate: true
		}, sourceLabel)), items.length > 1 ? /* @__PURE__ */ __dil.jsx("row", { flex: "none" }, /* @__PURE__ */ __dil.jsx("text", {
			color: "tertiary",
			inline: true,
			size: "3xs"
		}, "+", items.length - 1)) : null));
		if (!showPopover) return hasUrl ? /* @__PURE__ */ __dil.jsx("pressable", {
			ariaLabel: \\\`Open \\\${sourceLabel}\\\`,
			inline: true,
			onClick: () => GenUI.openUrl(url)
		}, badge) : badge;
		return /* @__PURE__ */ __dil.jsx("popover", {
			hoverOpenDelay: 80,
			showOnHover: true
		}, /* @__PURE__ */ __dil.jsx("popover-trigger", { onClick: () => !isMobile && isSm && hasUrl ? GenUI.openUrl(url) : void 0 }, badge), /* @__PURE__ */ __dil.jsx("popover-content", {
			align: "start",
			showCloseButton: isMobile ? false : void 0,
			side: "bottom",
			sideOffset: 8
		}, isMobile ? /* @__PURE__ */ __dil.jsx(MobileCitationSources, { items }) : /* @__PURE__ */ __dil.jsx(DesktopCitationSources, { items })));
	}
	function getCitationDetail(item) {
		return [item.metadata_label, item.snippet].filter(Boolean).join(" — ");
	}
	function DesktopCitationSources({ items }) {
		return /* @__PURE__ */ __dil.jsx("col", {
			gap: 0,
			maxHeight: 320,
			maxWidth: "100%",
			padding: {
				bottom: 2,
				top: 2,
				x: 5
			},
			scrollable: true,
			width: 384
		}, items.map((item, index) => {
			const itemUrl = item.url ?? "";
			const itemSourceLabel = item.source_label ?? item.title ?? "Source";
			const itemTitle = item.title ?? "";
			const itemDetail = getCitationDetail(item);
			const source = /* @__PURE__ */ __dil.jsx("col", {
				border: index === 0 ? void 0 : { top: {
					color: "default",
					size: 1
				} },
				gap: 1.5,
				padding: { y: 3 },
				width: "100%"
			}, /* @__PURE__ */ __dil.jsx("row", {
				align: "center",
				gap: 2,
				minWidth: 0,
				width: "100%"
			}, itemUrl ? /* @__PURE__ */ __dil.jsx("favicon", {
				size: 16,
				url: itemUrl
			}) : null, /* @__PURE__ */ __dil.jsx("text", {
				color: "secondary",
				inline: true,
				maxLines: 1,
				size: "xs",
				truncate: true
			}, itemSourceLabel)), itemTitle ? /* @__PURE__ */ __dil.jsx("text", {
				maxLines: 2,
				size: "sm",
				weight: "medium"
			}, itemTitle) : null, itemDetail || item.thumbnail_url ? /* @__PURE__ */ __dil.jsx("row", {
				align: "start",
				gap: 4,
				width: "100%"
			}, /* @__PURE__ */ __dil.jsx("col", {
				flex: 1,
				minWidth: 0
			}, itemDetail ? /* @__PURE__ */ __dil.jsx("text", {
				color: "secondary",
				maxLines: 3,
				size: "sm"
			}, itemDetail) : null), item.thumbnail_url ? /* @__PURE__ */ __dil.jsx("image", {
				alt: "",
				fit: "cover",
				height: 56,
				hideOnFailure: true,
				radius: "xl",
				src: item.thumbnail_url,
				width: 56
			}) : null) : null);
			return itemUrl ? /* @__PURE__ */ __dil.jsx("pressable", {
				ariaLabel: \\\`Open \\\${itemSourceLabel}\\\`,
				key: \\\`\\\${itemUrl}:\\\${index}\\\`,
				onClick: () => GenUI.openUrl(itemUrl),
				width: "100%"
			}, source) : /* @__PURE__ */ __dil.jsx("col", {
				key: \\\`source:\\\${index}\\\`,
				width: "100%"
			}, source);
		}));
	}
	function MobileCitationSources({ items }) {
		return /* @__PURE__ */ __dil.jsx("col", {
			gap: 3,
			maxHeight: 560,
			scrollable: true,
			width: "100%"
		}, items.map((item, index) => {
			const itemUrl = item.url ?? "";
			const itemSourceLabel = item.source_label ?? item.title ?? "Source";
			const itemTitle = item.title ?? "";
			const source = /* @__PURE__ */ __dil.jsx("col", {
				gap: 1,
				width: "100%"
			}, /* @__PURE__ */ __dil.jsx("row", {
				align: "center",
				gap: 2,
				minWidth: 0,
				width: "100%"
			}, itemUrl ? /* @__PURE__ */ __dil.jsx("favicon", {
				size: 16,
				url: itemUrl
			}) : null, /* @__PURE__ */ __dil.jsx("text", {
				size: "sm",
				weight: "semibold"
			}, itemSourceLabel)), itemTitle ? /* @__PURE__ */ __dil.jsx("text", { size: "sm" }, itemTitle) : null, item.metadata_label ? /* @__PURE__ */ __dil.jsx("caption", null, item.metadata_label) : null);
			return /* @__PURE__ */ __dil.jsx("col", {
				gap: 3,
				key: \\\`\\\${itemUrl}:\\\${index}\\\`,
				width: "100%"
			}, index > 0 ? /* @__PURE__ */ __dil.jsx("divider", { color: "subtle" }) : null, itemUrl ? /* @__PURE__ */ __dil.jsx("pressable", {
				ariaLabel: \\\`Open \\\${itemSourceLabel}\\\`,
				onClick: () => GenUI.openUrl(itemUrl),
				width: "100%"
			}, source) : source);
		}));
	}
	function Cite(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("Cite", props);
		if (!state) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("text", { inline: true }, /* @__PURE__ */ __dil.jsx("loading", { label: "Loading citations" }));
		const items = state.items ?? [];
		if (!items.length) return null;
		return /* @__PURE__ */ __dil.jsx("text", { inline: true }, /* @__PURE__ */ __dil.jsx(ResolvedCite, {
			activeIndex: state.active_index,
			items
		}));
	}

//#endregion
return Cite;
})();
return __dilDefaultExport;
})()))\`,CoTToolGroup:\`(() => {
var __dilDefaultExport = (function() {


//#region src/messages.dil.ts
	const fallbackMessages = { moreItems: "+{count} more" };
	function useMessages() {
		const messages = DIL.useAppData((data) => data.messages);
		return {
			...fallbackMessages,
			...messages
		};
	}
	function formatMessage(template, values = {}) {
		return template.replace(/\\\\{(\\\\w+)\\\\}/g, (placeholder, key) => values[key] === void 0 ? placeholder : String(values[key]));
	}

//#endregion
//#region src/SourceCards.dil.tsx
	const OVERFLOW_THRESHOLD = 8;
	const INITIAL_VISIBLE_COUNT = 6;
	function SourceCards({ children, label }) {
		const [expanded, setExpanded] = DIL.useState(false);
		const messages = useMessages();
		const visibleCount = expanded || children.length <= OVERFLOW_THRESHOLD ? children.length : INITIAL_VISIBLE_COUNT;
		return /* @__PURE__ */ __dil.jsx("carousel", {
			ariaLabel: label,
			gap: 2,
			flush: true,
			snap: "none"
		}, [...children.slice(0, visibleCount), visibleCount < children.length && /* @__PURE__ */ __dil.jsx("carousel-item", { key: "overflow" }, /* @__PURE__ */ __dil.jsx("pressable", {
			onClick: () => setExpanded(true),
			height: "100%",
			justify: "center",
			align: "start"
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "xs",
			color: "secondary"
		}, formatMessage(messages.moreItems, { count: children.length - visibleCount }))))]);
	}

//#endregion
//#region src/CalendarSources.dil.tsx
	function CalendarSources({ sources, label }) {
		return /* @__PURE__ */ __dil.jsx(SourceCards, { label }, sources.map((source, index) => /* @__PURE__ */ __dil.jsx("carousel-item", {
			key: \\\`\\\${source.url ?? source.title}-\\\${index}\\\`,
			maxWidth: 200
		}, source.url ? /* @__PURE__ */ __dil.jsx("link", {
			href: source.url,
			title: source.title
		}, /* @__PURE__ */ __dil.jsx(CalendarCard, { source })) : /* @__PURE__ */ __dil.jsx(CalendarCard, { source }))));
	}
	function CalendarCard({ source }) {
		const [hovered, setHovered] = DIL.useState(false);
		return /* @__PURE__ */ __dil.jsx("box", {
			radius: "lg",
			clip: true
		}, /* @__PURE__ */ __dil.jsx("badge", {
			color: hovered ? "var(--color-background-secondary-soft-alpha-hover)" : "secondary",
			variant: "soft",
			pill: false,
			padding: 0,
			onHover: source.url ? setHovered : void 0
		}, /* @__PURE__ */ __dil.jsx("row", {
			gap: 2,
			align: "stretch",
			padding: {
				top: 2,
				right: 2,
				bottom: 2,
				left: 1.5
			},
			maxWidth: 200
		}, /* @__PURE__ */ __dil.jsx("box", {
			width: 4,
			flex: "none",
			radius: "full",
			background: source.color ?? "info-solid",
			ariaHidden: true
		}), /* @__PURE__ */ __dil.jsx("col", {
			gap: .5,
			padding: { bottom: .5 },
			minWidth: 0,
			flex: 1
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "xs",
			color: "secondary",
			weight: "medium",
			truncate: true
		}, source.title), /* @__PURE__ */ __dil.jsx("text", {
			size: "2xs",
			color: "tertiary",
			truncate: true
		}, source.date && source.time ? \\\`\\\${source.date} • \\\${source.time}\\\` : source.date || source.time)))));
	}

//#endregion
//#region src/EmailSources.dil.tsx
	function EmailSources({ sources, label }) {
		return /* @__PURE__ */ __dil.jsx(SourceCards, { label }, sources.map((source, index) => /* @__PURE__ */ __dil.jsx("carousel-item", {
			key: \\\`\\\${source.url ?? source.subject}-\\\${index}\\\`,
			maxWidth: 200
		}, source.url ? /* @__PURE__ */ __dil.jsx("link", {
			href: source.url,
			title: source.subject
		}, /* @__PURE__ */ __dil.jsx(EmailCard, { source })) : /* @__PURE__ */ __dil.jsx(EmailCard, { source }))));
	}
	function EmailCard({ source }) {
		const [hovered, setHovered] = DIL.useState(false);
		return /* @__PURE__ */ __dil.jsx("box", {
			radius: "lg",
			clip: true
		}, /* @__PURE__ */ __dil.jsx("badge", {
			color: hovered ? "var(--color-background-secondary-soft-alpha-hover)" : "secondary",
			variant: "soft",
			pill: false,
			padding: 0,
			onHover: source.url ? setHovered : void 0
		}, /* @__PURE__ */ __dil.jsx("row", {
			gap: 2,
			align: "start",
			padding: 2,
			maxWidth: 200
		}, /* @__PURE__ */ __dil.jsx("box", {
			width: 20,
			height: 20,
			flex: "none",
			ariaHidden: true
		}, source.senderIcon ? /* @__PURE__ */ __dil.jsx("image", {
			src: source.senderIcon,
			alt: "",
			width: 20,
			height: 20,
			radius: "full",
			fit: "cover",
			fallback: /* @__PURE__ */ __dil.jsx(SenderInitial, { sender: source.sender })
		}) : /* @__PURE__ */ __dil.jsx(SenderInitial, { sender: source.sender })), /* @__PURE__ */ __dil.jsx("col", {
			gap: 0,
			minWidth: 0,
			flex: 1
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "xs",
			color: "secondary",
			weight: "medium",
			truncate: true
		}, source.subject), /* @__PURE__ */ __dil.jsx("text", {
			size: "2xs",
			color: "tertiary",
			truncate: true
		}, source.date ? \\\`\\\${source.date} • \\\${source.sender}\\\` : source.sender)))));
	}
	function SenderInitial({ sender }) {
		return /* @__PURE__ */ __dil.jsx("box", {
			width: 20,
			height: 20,
			radius: "full",
			background: "discovery-solid",
			align: "center",
			justify: "center"
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "2xs",
			color: "white",
			weight: "medium"
		}, sender.slice(0, 1).toUpperCase()));
	}

//#endregion
//#region src/WebSearch.dil.tsx
	function useSearchFaviconUrl(urls, isActive) {
		const now = DIL.useNow(isActive && (urls?.length ?? 0) > 1);
		const firstUrl = DIL.useRef(urls?.[0]);
		const startedAt = DIL.useRef(now);
		const index = DIL.useRef(isActive ? 0 : (urls?.length ?? 0) - 1);
		if (firstUrl.current !== urls?.[0]) {
			firstUrl.current = urls?.[0];
			startedAt.current = now;
			index.current = isActive ? 0 : (urls?.length ?? 0) - 1;
		}
		if (isActive && urls != null) index.current = Math.min(Math.floor((now - startedAt.current) / 1500), urls.length - 1);
		return urls?.[Math.min(index.current, urls.length - 1)];
	}
	function SearchFavicon({ url }) {
		return /* @__PURE__ */ __dil.jsx("box", {
			width: 20,
			height: 20,
			align: "stretch",
			justify: "center",
			flex: "0 0 auto",
			ariaHidden: true
		}, /* @__PURE__ */ __dil.jsx("transition", {
			initial: { opacity: 0 },
			enter: {
				opacity: 1,
				duration: 100,
				delay: 0
			},
			exit: {
				opacity: 0,
				duration: 100,
				delay: 0
			},
			preventInitialTransition: true
		}, url != null && /* @__PURE__ */ __dil.jsx("favicon", {
			key: url,
			url,
			size: 20,
			frame: true
		})));
	}
	function WebSearchDetails({ item }) {
		const [allQueries, setAllQueries] = DIL.useState(false);
		const [allResources, setAllResources] = DIL.useState(false);
		const queries = item.queries ?? [];
		const resources = item.resources?.filter((resource) => resource.type === "web_search_source") ?? [];
		const queryOverflow = queries.length > 3 && item.moreQueriesLabel != null;
		const resourceOverflow = resources.length > 8 && item.moreResourcesLabel != null;
		return /* @__PURE__ */ __dil.jsx("row", {
			gap: 2,
			wrap: "wrap",
			align: "center",
			width: "100%"
		}, resources.length > 0 ? /* @__PURE__ */ __dil.jsx(__dil.Fragment, null, (resourceOverflow && !allResources ? resources.slice(0, 7) : resources).map((source) => /* @__PURE__ */ __dil.jsx("link", {
			key: source.url,
			href: source.url,
			title: source.title
		}, /* @__PURE__ */ __dil.jsx(SearchPill, {
			key: \\\`source-pill-\\\${item.id}-\\\${source.url}\\\`,
			label: source.attribution,
			resources: [source],
			interactive: true
		}))), resourceOverflow && /* @__PURE__ */ __dil.jsx("pressable", {
			key: "resource-overflow",
			onClick: () => setAllResources(!allResources),
			ariaPressed: allResources
		}, /* @__PURE__ */ __dil.jsx(SearchPill, {
			label: allResources ? item.showLessLabel : item.moreResourcesLabel,
			resources: allResources ? void 0 : resources.slice(7, 10)
		}))) : /* @__PURE__ */ __dil.jsx(__dil.Fragment, null, (queryOverflow && !allQueries ? queries.slice(0, 2) : queries).map((query) => /* @__PURE__ */ __dil.jsx(SearchPill, {
			key: \\\`query-pill-\\\${item.id}-\\\${query}\\\`,
			label: query,
			query: true
		})), queryOverflow && /* @__PURE__ */ __dil.jsx("pressable", {
			key: "query-overflow",
			onClick: () => setAllQueries(!allQueries),
			ariaPressed: allQueries
		}, /* @__PURE__ */ __dil.jsx(SearchPill, { label: allQueries ? item.showLessLabel : item.moreQueriesLabel }))), item.pattern != null && /* @__PURE__ */ __dil.jsx(SearchPill, {
			label: item.pattern,
			query: true
		}));
	}
	function SearchPill({ label, resources, query = false, interactive = false }) {
		const [hovered, setHovered] = DIL.useState(false);
		return /* @__PURE__ */ __dil.jsx("badge", {
			size: "lg",
			padding: {
				left: query || resources?.length ? 2 : 3,
				right: 3
			},
			maxWidth: "100%",
			weight: "normal",
			color: hovered ? "var(--color-background-secondary-soft-alpha-hover)" : "secondary",
			variant: "soft",
			onHover: interactive ? setHovered : void 0
		}, /* @__PURE__ */ __dil.jsx("row", {
			gap: 1,
			align: "center",
			minWidth: 0
		}, query && /* @__PURE__ */ __dil.jsx("icon", {
			name: "search",
			size: "xs",
			color: "secondary"
		}), resources != null && /* @__PURE__ */ __dil.jsx("row", {
			gap: 0,
			flex: "none",
			ariaHidden: true
		}, resources.map((source, index) => /* @__PURE__ */ __dil.jsx("box", {
			key: source.url,
			width: index === resources.length - 1 ? 12 : 8,
			flex: "none"
		}, /* @__PURE__ */ __dil.jsx("favicon", {
			url: source.url,
			size: 12,
			frame: resources.length > 1
		})))), /* @__PURE__ */ __dil.jsx("box", {
			minWidth: 0,
			maxWidth: query ? "8rem" : void 0
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "xs",
			color: "secondary",
			truncate: true
		}, label))));
	}

//#endregion
//#region src/CoTToolGroup.dil.tsx
	const nativeIconNames = {
		api_tool: "plug",
		"book-open": "book-open",
		"code-searching": "search",
		"edit-files": "pencil",
		finances: "chart-no-axes-combined",
		globe: "globe",
		library: "library",
		"list-files": "folder",
		"run-command": "terminal"
	};
	function ToolIcon({ icon, expanded, faviconUrl }) {
		if (icon.type === "favicons") return /* @__PURE__ */ __dil.jsx(SearchFavicon, { url: faviconUrl });
		return /* @__PURE__ */ __dil.jsx("box", {
			width: 20,
			height: 20,
			align: "center",
			justify: "center",
			flex: "0 0 auto",
			ariaHidden: true
		}, icon.type === "native" ? /* @__PURE__ */ __dil.jsx("icon", {
			name: nativeIconNames[icon.name],
			size: "lg",
			color: expanded && icon.name !== "globe" ? "primary" : "secondary"
		}) : /* @__PURE__ */ __dil.jsx("image", {
			src: icon.src,
			alt: "",
			width: 20,
			height: 20,
			fit: "contain",
			radius: icon.kind === "favicon" ? "full" : void 0,
			frame: icon.kind === "favicon",
			fallback: /* @__PURE__ */ __dil.jsx("icon", {
				name: icon.kind === "favicon" ? "globe" : "plug",
				size: "lg",
				color: "secondary"
			})
		}));
	}
	function ActivityLabel({ title, isActive, icon, expanded, onToggle }) {
		const [hovered, setHovered] = DIL.useState(false);
		const faviconUrl = useSearchFaviconUrl(icon?.type === "favicons" ? icon.urls : void 0, isActive);
		const Container = onToggle != null ? "pressable" : "box";
		const color = onToggle != null && hovered ? "primary" : "tertiary";
		return /* @__PURE__ */ __dil.jsx(Container, {
			direction: "row",
			align: "center",
			gap: 2,
			maxWidth: "100%",
			ariaLabel: onToggle != null ? title : void 0,
			onClick: onToggle,
			onHover: onToggle != null ? setHovered : void 0
		}, icon != null && /* @__PURE__ */ __dil.jsx(ToolIcon, {
			icon,
			expanded,
			faviconUrl
		}), /* @__PURE__ */ __dil.jsx("row", {
			gap: 1,
			align: "center",
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("box", {
			minWidth: 0,
			ariaHidden: onToggle != null
		}, isActive && (onToggle == null || !hovered) ? /* @__PURE__ */ __dil.jsx("shimmer-text", {
			size: "md",
			color,
			weight: "normal",
			truncate: true
		}, title) : /* @__PURE__ */ __dil.jsx("text", {
			size: "md",
			color,
			weight: "normal",
			truncate: true
		}, title)), onToggle != null && /* @__PURE__ */ __dil.jsx("box", {
			width: 16,
			height: 16,
			ariaHidden: true
		}, /* @__PURE__ */ __dil.jsx("transition", {
			initial: {
				rotate: expanded ? -90 : 90,
				opacity: 1
			},
			enter: {
				rotate: 0,
				opacity: 1,
				duration: 150,
				delay: 0
			},
			exit: {
				opacity: 0,
				duration: 0,
				delay: 0
			},
			layoutEnter: {
				duration: 0,
				delay: 0
			},
			layoutExit: {
				duration: 0,
				delay: 0
			},
			layoutMove: {
				duration: 0,
				delay: 0
			},
			preventInitialTransition: true
		}, /* @__PURE__ */ __dil.jsx("box", {
			key: expanded ? "expanded" : "collapsed",
			width: 16,
			height: 16,
			align: "center",
			justify: "center"
		}, (expanded || hovered) && /* @__PURE__ */ __dil.jsx("icon", {
			name: expanded ? "chevron-down" : "chevron-right",
			size: "sm",
			color
		}))))));
	}
	function CoTToolGroup() {
		const data = DIL.useAppData((data$1) => data$1);
		const [expanded, setExpanded] = DIL.useState(false);
		const headerItem = data.isActive ? data.items[data.items.length - 1] : void 0;
		const currentStatus = data.isActive ? data.current_status : void 0;
		const singleItem = data.items.length === 1 ? data.items[0] : null;
		if (singleItem != null) return /* @__PURE__ */ __dil.jsx(Activity, {
			key: singleItem.id,
			item: singleItem,
			currentStatus,
			isActive: data.isActive,
			autoExpand: data.isActive,
			indentDetails: true
		});
		const groupContent = expanded ? /* @__PURE__ */ __dil.jsx(NestedContent, {
			key: "tool-group-content",
			indentItems: true
		}, data.items.map((item, index) => /* @__PURE__ */ __dil.jsx(Activity, {
			key: item.id,
			item,
			isActive: item.isActive,
			autoExpand: data.isActive && index === data.items.length - 1
		}))) : headerItem != null && hasResourceCards(headerItem) ? /* @__PURE__ */ __dil.jsx(NestedContent, { key: \\\`tool-group-header-details-\\\${headerItem.id}\\\` }, /* @__PURE__ */ __dil.jsx(ActivityDetails, {
			item: headerItem,
			topPadding: 0,
			withGuide: true
		})) : false;
		return /* @__PURE__ */ __dil.jsx("col", {
			gap: 0,
			align: "start",
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx(ActivityLabel, {
			title: currentStatus ?? data.title,
			isActive: data.isActive,
			icon: data.icon,
			expanded,
			onToggle: data.items.length > 0 ? () => setExpanded(!expanded) : void 0
		}), /* @__PURE__ */ __dil.jsx(ExpandableContent, {
			revealAfterResize: headerItem != null && hasResourceCards(headerItem),
			slideUpOnEnter: !expanded && headerItem != null && hasResourceCards(headerItem),
			slideDownOnExit: expanded && headerItem != null && hasResourceCards(headerItem)
		}, groupContent));
	}
	function Activity({ item, currentStatus, isActive, autoExpand = false, indentDetails = false }) {
		const [userExpanded, setUserExpanded] = DIL.useState(false);
		const hasDetails = hasActivityDetails(item);
		const expanded = hasDetails && (autoExpand || userExpanded);
		return /* @__PURE__ */ __dil.jsx("col", {
			gap: 0,
			align: "start",
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx(ActivityLabel, {
			title: currentStatus ?? item.label,
			isActive,
			icon: item.icon,
			expanded,
			onToggle: hasDetails && !autoExpand ? () => setUserExpanded(!userExpanded) : void 0
		}), hasDetails && /* @__PURE__ */ __dil.jsx(ExpandableContent, null, expanded && /* @__PURE__ */ __dil.jsx(NestedContent, {
			key: "activity-details",
			topPadding: indentDetails ? 3 : 0,
			bottomPadding: indentDetails ? 3 : 0,
			showGuide: indentDetails
		}, /* @__PURE__ */ __dil.jsx(ActivityDetails, {
			item,
			topPadding: indentDetails ? 0 : 3,
			withGuide: indentDetails
		}))));
	}
	function NestedContent({ children, topPadding = 3, bottomPadding = 0, indentItems = false, showGuide = true }) {
		return /* @__PURE__ */ __dil.jsx("row", {
			width: "100%",
			gap: showGuide ? 1 : 0,
			align: "stretch",
			padding: {
				top: topPadding,
				bottom: bottomPadding
			}
		}, /* @__PURE__ */ __dil.jsx("row", {
			width: 20,
			flex: "0 0 auto",
			justify: "center",
			gap: 0,
			ariaHidden: true
		}, showGuide && /* @__PURE__ */ __dil.jsx("divider", {
			color: "default",
			spacing: 0
		})), /* @__PURE__ */ __dil.jsx("col", {
			gap: 3,
			flex: 1,
			minWidth: 0,
			padding: {
				left: indentItems ? 2 : 0,
				bottom: indentItems ? 2 : 0
			}
		}, children));
	}
	function ActivityDetails({ item, topPadding = 3, withGuide }) {
		const search = item.type === "web_search" ? item : void 0;
		const emails = item.resources?.filter((resource) => resource.type === "email_source");
		const events = item.resources?.filter((resource) => resource.type === "calendar_source");
		const hasSourceCards = !!emails?.length || !!events?.length;
		return /* @__PURE__ */ __dil.jsx("box", {
			padding: {
				top: topPadding,
				left: hasSourceCards || search != null ? withGuide ? 1 : 2 : 0,
				right: hasSourceCards ? 4 : 0
			},
			width: "100%"
		}, search != null && /* @__PURE__ */ __dil.jsx(WebSearchDetails, { item: search }), !!emails?.length && /* @__PURE__ */ __dil.jsx(EmailSources, {
			sources: emails,
			label: item.label
		}), !!events?.length && /* @__PURE__ */ __dil.jsx(CalendarSources, {
			sources: events,
			label: item.label
		}));
	}
	function hasActivityDetails(item) {
		return hasResourceCards(item) || item.type === "web_search" && (!!item.queries?.length || item.pattern != null);
	}
	function hasResourceCards(item) {
		return !!item.resources?.some((resource) => resource.type === "email_source" || resource.type === "calendar_source" || item.type === "web_search" && resource.type === "web_search_source");
	}
	function ExpandableContent({ children, revealAfterResize = false, slideUpOnEnter = false, slideDownOnExit = false }) {
		return /* @__PURE__ */ __dil.jsx("box", { width: "100%" }, /* @__PURE__ */ __dil.jsx("transition", {
			dimension: "height",
			initial: {
				opacity: revealAfterResize ? 0 : 1,
				translateY: slideUpOnEnter ? 4 : 0
			},
			enter: {
				opacity: 1,
				translateY: 0,
				duration: slideUpOnEnter ? 200 : revealAfterResize ? 150 : 0,
				delay: slideUpOnEnter ? 100 : revealAfterResize ? 300 : 0
			},
			exit: {
				opacity: revealAfterResize ? 0 : 1,
				translateY: slideDownOnExit ? 4 : 0,
				duration: slideUpOnEnter ? 100 : slideDownOnExit ? 150 : 0,
				delay: 0
			},
			layoutEnter: {
				duration: 300,
				delay: 0,
				timingFunction: "ease-in-out"
			},
			layoutExit: {
				duration: 300,
				delay: 0,
				timingFunction: "ease-in-out"
			},
			layoutMove: {
				duration: revealAfterResize ? 300 : 0,
				delay: 0,
				timingFunction: "ease-in-out"
			},
			hideOverflow: true,
			preventInitialTransition: true
		}, children));
	}

//#endregion
return CoTToolGroup;
})();
return __dilDefaultExport;
})()\`,CodeBlock:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentCodeBlock(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "CodeBlock",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "CodeBlock", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentCodeBlock, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region CodeBlock.dil.tsx
	function CodeBlock({ content, language }) {
		return /* @__PURE__ */ __dil.jsx("card", { size: "full" }, /* @__PURE__ */ __dil.jsx("col", { gap: 2 }, /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			size: "sm",
			weight: "medium"
		}, language ?? "code"), /* @__PURE__ */ __dil.jsx("box", {
			background: "surface-secondary",
			padding: 3,
			radius: "md"
		}, /* @__PURE__ */ __dil.jsx("text", {
			preserveWhitespace: true,
			size: "sm"
		}, content))));
	}

//#endregion
return CodeBlock;
})();
return __dilDefaultExport;
})()))\`,CodeCite:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentCodeCite(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "CodeCite",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "CodeCite", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentCodeCite, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region CodeCite.dil.tsx
	function ResolvedCodeCite({ componentKey, state }) {
		const isMobile = DIL.useIsMobile();
		const [hovered, setHovered] = DIL.useState(false);
		const label = state.display_label;
		const openCitation = () => GenUI.dispatchAction({
			handler: "client",
			type: "open_coding_citation",
			payload: { component_key: componentKey }
		});
		return /* @__PURE__ */ __dil.jsx("popover", {
			hoverOpenDelay: 0,
			showOnHover: true
		}, /* @__PURE__ */ __dil.jsx("popover-trigger", { onClick: openCitation }, /* @__PURE__ */ __dil.jsx("badge", {
			color: hovered ? {
				light: "#e8e8e8",
				dark: "gray-300"
			} : {
				light: "gray-75",
				dark: "gray-400"
			},
			onHover: setHovered,
			padding: {
				x: 1,
				y: 1
			},
			size: "sm",
			variant: "soft",
			weight: "normal"
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			ariaLabel: label,
			role: "img"
		}, /* @__PURE__ */ __dil.jsx("icon", {
			color: "secondary",
			name: "file-code",
			size: "md"
		})))), /* @__PURE__ */ __dil.jsx("popover-content", {
			align: "start",
			side: "bottom",
			sideOffset: 4
		}, /* @__PURE__ */ __dil.jsx("col", {
			gap: 3,
			maxWidth: "100%",
			padding: 3,
			width: isMobile ? "100%" : 324
		}, /* @__PURE__ */ __dil.jsx("pressable", {
			ariaLabel: label,
			gap: 2,
			onClick: openCitation
		}, /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			size: "xs",
			weight: "medium"
		}, state.preview_label), /* @__PURE__ */ __dil.jsx("divider", null), /* @__PURE__ */ __dil.jsx("box", {
			background: "surface-tertiary",
			padding: 2,
			radius: "md"
		}, /* @__PURE__ */ __dil.jsx("code", null, state.highlighted_text))))));
	}
	function CodeCite(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("CodeCite", props);
		if (!state) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("loading", { label: "Loading code citation" });
		return state.highlighted_text ? /* @__PURE__ */ __dil.jsx(ResolvedCodeCite, {
			componentKey: props.__resolutionId ?? componentDataKey("CodeCite", props),
			state
		}) : null;
	}

//#endregion
return CodeCite;
})();
return __dilDefaultExport;
})()))\`,Entity:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentEntity(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "Entity",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "Entity", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentEntity, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region Entity.dil.tsx
	function Entity(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("Entity", props);
		const isMobile = DIL.useIsMobile();
		if (!state) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("loading", { label: "Loading entity" });
		const onClick = () => state.click_action ? GenUI.dispatchAction(state.click_action) : state.action_payload ? GenUI.dispatchAction({
			handler: "client",
			type: "open_entity_detail",
			payload: state.action_payload
		}) : GenUI.openEntityDetail(props.category ?? "ask_sidebar", props.query ?? state.value);
		const entity = state.entity_data;
		if (!isMobile && state.action_payload != null && entity != null) {
			const name = entity.name || state.value;
			const categories = entity.categories?.filter(Boolean) ?? [];
			const hasRating = typeof entity.rating === "number" && Number.isFinite(entity.rating);
			const hasPrice = Boolean(entity.price_str);
			const hasOpenStatus = typeof entity.is_open === "boolean";
			const directionsUrl = state.directions_url;
			const legacyWebsiteUrl = entity.website_url;
			const legacyWebsiteMatch = typeof legacyWebsiteUrl === "string" ? /^https:\\\\/\\\\/(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\\\.)+(?:[a-z]{2,63}|xn--[a-z0-9](?:[a-z0-9-]{0,57}[a-z0-9])?)(?::(\\\\d{1,5}))?(?:[/?#][^\\\\s\\\\\\\\]*)?$/i.exec(legacyWebsiteUrl) : null;
			const websiteUrl = state.website_url ?? (legacyWebsiteMatch && (!legacyWebsiteMatch[1] || Number(legacyWebsiteMatch[1]) <= 65535) ? legacyWebsiteUrl : void 0);
			return /* @__PURE__ */ __dil.jsx("popover", {
				hoverOpenDelay: 150,
				showOnHover: true
			}, /* @__PURE__ */ __dil.jsx("popover-trigger", { onClick }, /* @__PURE__ */ __dil.jsx("text", {
				inline: true,
				underline: "dotted"
			}, state.value)), /* @__PURE__ */ __dil.jsx("popover-content", {
				align: "center",
				side: "top",
				sideOffset: 8
			}, /* @__PURE__ */ __dil.jsx("col", {
				gap: 3,
				maxWidth: "100%",
				padding: 4,
				width: 420
			}, /* @__PURE__ */ __dil.jsx("row", {
				align: "start",
				gap: 3,
				wrap: "nowrap"
			}, state.image_url ? /* @__PURE__ */ __dil.jsx("pressable", {
				ariaLabel: \\\`View details for \\\${name}\\\`,
				onClick
			}, /* @__PURE__ */ __dil.jsx("image", {
				alt: name,
				fit: "cover",
				height: 96,
				hideOnFailure: true,
				radius: "xl",
				src: state.image_url,
				width: 96
			})) : null, /* @__PURE__ */ __dil.jsx("col", {
				flex: 1,
				gap: 1,
				minWidth: 0
			}, /* @__PURE__ */ __dil.jsx("pressable", {
				ariaLabel: \\\`View details for \\\${name}\\\`,
				maxWidth: "100%",
				onClick,
				width: "fit-content"
			}, /* @__PURE__ */ __dil.jsx("text", {
				inline: true,
				maxLines: 1,
				size: "lg",
				weight: "semibold"
			}, name)), hasRating || categories.length > 0 ? /* @__PURE__ */ __dil.jsx("row", {
				align: "center",
				gap: 1,
				wrap: "wrap"
			}, hasRating ? /* @__PURE__ */ __dil.jsx("row", {
				align: "center",
				gap: 1,
				wrap: "nowrap"
			}, /* @__PURE__ */ __dil.jsx("icon", {
				color: "primary",
				name: "star-filled",
				size: "sm"
			}), /* @__PURE__ */ __dil.jsx("text", {
				inline: true,
				size: "md"
			}, Math.round(entity.rating * 10) / 10)) : null, hasRating && categories.length > 0 ? /* @__PURE__ */ __dil.jsx("text", {
				color: "secondary",
				inline: true,
				size: "md"
			}, "•") : null, categories.length > 0 ? /* @__PURE__ */ __dil.jsx("text", {
				inline: true,
				maxLines: 1,
				size: "md"
			}, categories.join(", ")) : null) : null, hasPrice || hasOpenStatus ? /* @__PURE__ */ __dil.jsx("row", {
				align: "center",
				gap: 1,
				wrap: "wrap"
			}, hasPrice ? /* @__PURE__ */ __dil.jsx("text", {
				inline: true,
				size: "md"
			}, entity.price_str) : null, hasPrice && hasOpenStatus ? /* @__PURE__ */ __dil.jsx("text", {
				color: "secondary",
				inline: true,
				size: "md"
			}, "•") : null, hasOpenStatus ? /* @__PURE__ */ __dil.jsx("text", {
				color: entity.is_open ? "success" : "secondary",
				inline: true,
				size: "md"
			}, entity.is_open ? "Open" : "Closed") : null) : null)), directionsUrl || websiteUrl ? /* @__PURE__ */ __dil.jsx("row", {
				align: "center",
				gap: 2,
				width: "100%",
				wrap: "nowrap"
			}, directionsUrl ? /* @__PURE__ */ __dil.jsx("button", {
				block: true,
				color: "secondary",
				onClick: () => GenUI.openUrl(directionsUrl),
				size: "md",
				variant: "outline"
			}, "Directions") : null, websiteUrl ? /* @__PURE__ */ __dil.jsx("button", {
				block: true,
				color: "secondary",
				onClick: () => GenUI.openUrl(websiteUrl),
				size: "md",
				variant: "outline"
			}, "Website") : null) : null)));
		}
		return /* @__PURE__ */ __dil.jsx("pressable", {
			inline: true,
			onClick
		}, /* @__PURE__ */ __dil.jsx("text", {
			inline: true,
			underline: "dotted"
		}, state.value));
	}

//#endregion
return Entity;
})();
return __dilDefaultExport;
})()))\`,FileCite:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentFileCite(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "FileCite",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "FileCite", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentFileCite, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region FileCite.dil.tsx
	function storedLocationLabel(payload) {
		const page = payload?.page_range_start;
		const lastPage = payload?.page_range_end;
		if (typeof page === "number" && Number.isInteger(page) && page > 0) return typeof lastPage === "number" && Number.isInteger(lastPage) && lastPage > page ? \\\`Pages \\\${page}–\\\${lastPage}\\\` : \\\`Page \\\${page}\\\`;
		const line = payload?.input_pointer?.line_range_start;
		const lastLine = payload?.input_pointer?.line_range_end;
		if (typeof line === "number" && Number.isInteger(line) && line >= 0) return typeof lastLine === "number" && Number.isInteger(lastLine) && lastLine > line ? \\\`Lines \\\${line}–\\\${lastLine}\\\` : \\\`Line \\\${line}\\\`;
		return null;
	}
	function ResolvedFileCite({ state }) {
		const isMobile = DIL.useIsMobile();
		const isSm = DIL.useBreakpoint("sm");
		const [hovered, setHovered] = DIL.useState(false);
		const name = state.file_name ?? "Document";
		const payload = state.file_action_payload;
		const snippet = typeof payload?.snippet === "string" ? payload.snippet : typeof payload?.text === "string" ? payload.text : null;
		const locationLabel = storedLocationLabel(payload);
		const openFile = () => {
			if (payload) GenUI.dispatchAction({
				handler: "client",
				type: "open_file_citation",
				payload
			});
			else if (state.safe_file_url) GenUI.openUrl(state.safe_file_url);
		};
		return /* @__PURE__ */ __dil.jsx("popover", {
			hoverOpenDelay: 80,
			showOnHover: true
		}, /* @__PURE__ */ __dil.jsx("popover-trigger", { onClick: () => !isMobile && isSm ? openFile() : void 0 }, /* @__PURE__ */ __dil.jsx("badge", {
			color: hovered ? {
				light: "#e8e8e8",
				dark: "gray-300"
			} : {
				light: "gray-75",
				dark: "gray-400"
			},
			onHover: setHovered,
			padding: {
				left: 1,
				right: 1.5,
				y: 1
			},
			size: "sm",
			variant: "soft",
			weight: "normal"
		}, /* @__PURE__ */ __dil.jsx("icon", {
			color: "secondary",
			name: "document",
			size: "sm"
		}), /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			inline: true,
			maxLines: 1,
			size: "3xs",
			truncate: true
		}, name))), /* @__PURE__ */ __dil.jsx("popover-content", {
			align: "start",
			side: "bottom",
			sideOffset: 8
		}, /* @__PURE__ */ __dil.jsx("col", {
			gap: 2,
			maxWidth: "100%",
			padding: 4,
			width: isMobile ? "100%" : 345
		}, /* @__PURE__ */ __dil.jsx("pressable", {
			ariaLabel: \\\`Open \\\${name}\\\`,
			gap: 2,
			onClick: openFile
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 2
		}, /* @__PURE__ */ __dil.jsx("icon", {
			color: "secondary",
			name: "document",
			size: "md"
		}), /* @__PURE__ */ __dil.jsx("text", {
			maxLines: 2,
			size: "sm",
			weight: "semibold"
		}, name)), locationLabel ? /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			size: "xs"
		}, locationLabel) : null, snippet ? /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			maxLines: 6,
			size: "sm"
		}, snippet) : null))));
	}
	function FileCite(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("FileCite", props);
		if (!state) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("loading", { label: "Loading file citation" });
		return state.file_name ? /* @__PURE__ */ __dil.jsx(ResolvedFileCite, { state }) : null;
	}

//#endregion
return FileCite;
})();
return __dilDefaultExport;
})()))\`,FileNavList:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentFileNavList(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "FileNavList",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "FileNavList", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentFileNavList, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region FileNavList.dil.tsx
	function ResolvedFileNavList({ componentKey, items }) {
		const isSm = DIL.useBreakpoint("sm");
		return /* @__PURE__ */ __dil.jsx("row", {
			gap: 2,
			width: "100%",
			wrap: "wrap"
		}, items.map((item) => /* @__PURE__ */ __dil.jsx("pressable", {
			ariaLabel: item.name,
			key: item.id + ":" + item.item_index,
			onClick: () => GenUI.dispatchAction({
				handler: "client",
				type: "open_file_navlist_item",
				payload: {
					component_key: componentKey,
					item_index: item.item_index
				}
			}),
			width: isSm ? 320 : 205
		}, /* @__PURE__ */ __dil.jsx("card", {
			background: "surface-primary",
			padding: 2.5,
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 2,
			minWidth: 0,
			width: "100%",
			wrap: "nowrap"
		}, /* @__PURE__ */ __dil.jsx("box", {
			align: "center",
			ariaHidden: true,
			flex: "0 0 auto",
			height: 40,
			justify: "center",
			width: 40
		}, /* @__PURE__ */ __dil.jsx("icon", {
			color: "secondary",
			name: "document",
			size: "2xl"
		})), /* @__PURE__ */ __dil.jsx("col", {
			flex: 1,
			gap: .5,
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("text", {
			maxLines: 1,
			size: "sm",
			truncate: true,
			weight: "semibold"
		}, item.name), /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			maxLines: 1,
			size: "sm",
			truncate: true
		}, item.description)))))));
	}
	function FileNavList(props) {
		const hideInaccessibleFiles = DIL.useAppData((appData) => appData.opGenui?.hideInaccessibleFiles === true);
		const { resolutionComplete, state } = useResolvedComponentResult("FileNavList", props);
		if (!state) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("loading", { label: "Loading files" });
		const items = state.items?.map((item, item_index) => ({
			...item,
			item_index
		})).filter((item) => item.id && item.name && item.is_actionable && (!hideInaccessibleFiles || item.safe_file_url)) ?? [];
		if (items.length === 0) return null;
		return /* @__PURE__ */ __dil.jsx(ResolvedFileNavList, {
			componentKey: componentDataKey("FileNavList", props),
			items
		});
	}

//#endregion
return FileNavList;
})();
return __dilDefaultExport;
})()))\`,FlightCard:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentFlightCard(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "FlightCard",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "FlightCard", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentFlightCard, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region FlightCard.dil.tsx
	function FlightCard(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("FlightCard", props);
		const isMobile = DIL.useIsMobile();
		if (!state) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("loading", { label: "Loading flight" });
		return /* @__PURE__ */ __dil.jsx("card", {
			background: "surface",
			gap: 4,
			padding: 5,
			size: "sm"
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "start",
			gap: 3,
			justify: "between",
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("col", {
			flex: 1,
			gap: .5,
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 1.5,
			minWidth: 0
		}, state.airline_logo_url ? /* @__PURE__ */ __dil.jsx("image", {
			alt: state.airline_logo_alt,
			fit: "contain",
			height: 18,
			hideOnFailure: true,
			radius: "full",
			src: state.airline_logo_url,
			width: 18
		}) : null, /* @__PURE__ */ __dil.jsx("text", {
			maxLines: 1,
			size: "md",
			truncate: true,
			weight: "medium"
		}, state.airline_name)), state.operating_airlines_label ? /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			size: "sm"
		}, state.operating_airlines_label) : null, /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			maxLines: 1,
			size: "sm",
			truncate: true
		}, state.trip_summary_label)), state.price_label ? /* @__PURE__ */ __dil.jsx("text", {
			maxLines: 1,
			size: "lg",
			textAlign: "end",
			weight: "semibold"
		}, state.price_label) : null), /* @__PURE__ */ __dil.jsx("row", {
			align: "start",
			gap: 3,
			width: "100%",
			wrap: "nowrap"
		}, /* @__PURE__ */ __dil.jsx("col", {
			flex: 1,
			gap: .5,
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("title", {
			size: isMobile ? "md" : "lg",
			weight: "semibold"
		}, state.departure_time_label), /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			size: "sm",
			weight: "medium"
		}, state.departure_airport_code)), /* @__PURE__ */ __dil.jsx("col", {
			align: "center",
			flex: 1,
			gap: 0,
			justify: "center",
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 1,
			height: 25,
			justify: "between",
			width: "100%"
		}, [
			...[
				0,
				1,
				2
			].map((index) => /* @__PURE__ */ __dil.jsx("box", {
				background: "border-subtle",
				key: \\\`outbound-dot-\\\${index}\\\`,
				radius: "full",
				size: 4
			})),
			...state.duration_label ? [/* @__PURE__ */ __dil.jsx("box", { key: "flight-duration" }, /* @__PURE__ */ __dil.jsx("text", {
				maxLines: 1,
				size: "xs",
				weight: "medium"
			}, state.duration_label))] : [],
			...[
				0,
				1,
				2
			].map((index) => /* @__PURE__ */ __dil.jsx("box", {
				background: "border-subtle",
				key: \\\`arrival-dot-\\\${index}\\\`,
				radius: "full",
				size: 4
			}))
		])), /* @__PURE__ */ __dil.jsx("col", {
			align: "end",
			flex: 1,
			gap: .5,
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("title", {
			size: isMobile ? "md" : "lg",
			textAlign: "end",
			weight: "semibold"
		}, state.arrival_time_label), /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			size: "sm",
			textAlign: "end",
			weight: "medium"
		}, state.arrival_airport_code))), state.booking_urls.length ? /* @__PURE__ */ __dil.jsx("col", {
			gap: 4,
			width: "100%"
		}, state.booking_urls.map((url) => /* @__PURE__ */ __dil.jsx("button", {
			block: true,
			color: "secondary",
			key: url,
			onClick: () => GenUI.openUrl(url),
			pill: true,
			size: "lg",
			variant: "outline"
		}, state.select_flight_label))) : null);
	}

//#endregion
return FlightCard;
})();
return __dilDefaultExport;
})()))\`,FlightCarousel:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentFlightCarousel(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "FlightCarousel",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "FlightCarousel", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentFlightCarousel, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region FlightCarousel.dil.tsx
	function FlightCarousel(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("FlightCarousel", props);
		const isMobile = DIL.useIsMobile();
		if (!state) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("loading", { label: "Loading flights" });
		return /* @__PURE__ */ __dil.jsx("carousel", {
			ariaLabel: state.aria_label,
			gap: 4
		}, state.items.map((item) => /* @__PURE__ */ __dil.jsx("carousel-item", {
			key: item.candidate_id,
			variant: "none",
			width: isMobile ? "84%" : 360
		}, /* @__PURE__ */ __dil.jsx("card", {
			background: "surface",
			gap: 4,
			height: "100%",
			padding: 5,
			size: "sm",
			width: "100%"
		}, item.title ? /* @__PURE__ */ __dil.jsx("title", {
			size: "lg",
			weight: "semibold"
		}, item.title) : null, /* @__PURE__ */ __dil.jsx("col", {
			border: { left: {
				size: 1,
				color: "subtle"
			} },
			gap: 2,
			padding: { left: 3 },
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 1.5,
			minWidth: 0
		}, item.airline_logo_url ? /* @__PURE__ */ __dil.jsx("image", {
			alt: item.airline_logo_alt,
			fit: "contain",
			height: 18,
			hideOnFailure: true,
			radius: "full",
			src: item.airline_logo_url,
			width: 18
		}) : null, /* @__PURE__ */ __dil.jsx("text", {
			maxLines: 1,
			size: "md",
			truncate: true
		}, item.airline_name), item.duration_label ? /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			maxLines: 1,
			size: "md"
		}, "· " + item.duration_label) : null), item.operating_airlines_label ? /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			size: "sm"
		}, item.operating_airlines_label) : null, /* @__PURE__ */ __dil.jsx("title", {
			size: "lg",
			weight: "semibold"
		}, item.departure_time_label + " → " + item.arrival_time_label), item.price_label ? /* @__PURE__ */ __dil.jsx("text", { size: "md" }, item.price_label) : null), item.commentary ? /* @__PURE__ */ __dil.jsx("text", { size: "md" }, item.commentary) : null, item.booking_urls.length ? /* @__PURE__ */ __dil.jsx("col", {
			flex: 1,
			gap: 4,
			justify: "end",
			width: "100%"
		}, item.booking_urls.map((url) => /* @__PURE__ */ __dil.jsx("button", {
			block: true,
			color: "secondary",
			key: url,
			onClick: () => GenUI.openUrl(url),
			pill: true,
			size: "lg",
			variant: "outline"
		}, state.select_flight_label))) : null))));
	}

//#endregion
return FlightCarousel;
})();
return __dilDefaultExport;
})()))\`,FlightTracker:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentFlightTracker(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "FlightTracker",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "FlightTracker", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentFlightTracker, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region FlightTracker.dil.tsx
	function FlightDetails({ details, isDesktop, arrival }) {
		return /* @__PURE__ */ __dil.jsx("grid", {
			columns: isDesktop ? 3 : [
				1.4,
				1,
				1
			],
			gap: isDesktop ? 4 : 3
		}, details.map((detail, index) => {
			const alignEnd = isDesktop ? arrival : index > 0;
			return /* @__PURE__ */ __dil.jsx("grid-item", { key: index }, /* @__PURE__ */ __dil.jsx("col", {
				gap: .5,
				align: alignEnd ? "end" : "start"
			}, /* @__PURE__ */ __dil.jsx("caption", {
				size: "sm",
				color: "secondary",
				textAlign: alignEnd ? "end" : "start"
			}, detail.title), /* @__PURE__ */ __dil.jsx("text", {
				size: "sm",
				weight: "medium",
				textAlign: alignEnd ? "end" : "start"
			}, detail.value)));
		}));
	}
	function ResolvedFlightTracker({ state }) {
		const isDesktop = DIL.useBreakpoint("sm");
		const completedDots = isDesktop ? state.completed_progress_dot_count : state.mobile_completed_progress_dot_count;
		const progressLeft = isDesktop ? state.flight_progress_left : state.mobile_flight_progress_left;
		const progressDots = isDesktop ? [
			0,
			1,
			2,
			3,
			4,
			5,
			6,
			7,
			8,
			9
		] : [
			0,
			1,
			2,
			3,
			4,
			5,
			6,
			7
		];
		const departureDetails = [
			{
				title: state.departure_detail_title,
				value: state.departure_detail_value
			},
			{
				title: state.departure_terminal_title,
				value: state.departure_terminal_value
			},
			{
				title: state.departure_gate_title,
				value: state.departure_gate_value
			}
		];
		const arrivalDetails = [
			{
				title: state.arrival_detail_title,
				value: state.arrival_detail_value
			},
			{
				title: state.arrival_terminal_title,
				value: state.arrival_terminal_value
			},
			{
				title: state.arrival_gate_title,
				value: state.arrival_gate_value
			}
		];
		return /* @__PURE__ */ __dil.jsx("card", {
			background: "surface",
			size: "full",
			padding: 0
		}, /* @__PURE__ */ __dil.jsx("col", { gap: 0 }, /* @__PURE__ */ __dil.jsx("box", { padding: {
			x: 5,
			y: 4
		} }, /* @__PURE__ */ __dil.jsx("col", { gap: 3 }, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 2
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 2,
			flex: "auto",
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "sm",
			weight: "medium",
			truncate: true
		}, state.header_label)), /* @__PURE__ */ __dil.jsx("badge", {
			color: state.badge_color,
			variant: "soft",
			size: "sm"
		}, state.badge_label)), /* @__PURE__ */ __dil.jsx("col", { gap: 1.5 }, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 4
		}, /* @__PURE__ */ __dil.jsx("title", {
			size: isDesktop ? "xl" : "lg",
			weight: "medium"
		}, state.origin_code), /* @__PURE__ */ __dil.jsx("col", {
			flex: "auto",
			align: "center",
			justify: "center",
			gap: 0,
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			justify: "center",
			width: "100%",
			height: 28
		}, /* @__PURE__ */ __dil.jsx("box", {
			width: "100%",
			maxWidth: "400px",
			height: "100%",
			gap: 0
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			justify: "between",
			width: "100%",
			height: "100%"
		}, progressDots.flatMap((item) => [!state.show_progress_plane && state.progress_center_label !== "" && item === progressDots.length / 2 && /* @__PURE__ */ __dil.jsx("box", {
			key: "duration",
			background: "surface",
			padding: { x: 1 },
			flex: "none"
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "xs",
			weight: "medium"
		}, state.progress_center_label)), state.show_progress_plane && progressLeft !== "" && item === completedDots ? /* @__PURE__ */ __dil.jsx("box", {
			key: item,
			width: 4,
			height: 28,
			align: "center",
			justify: "center"
		}, /* @__PURE__ */ __dil.jsx("box", {
			width: 28,
			height: 28,
			align: "center",
			justify: "center",
			background: "surface",
			radius: "full"
		}, /* @__PURE__ */ __dil.jsx("icon", {
			name: "plane",
			size: "2xl",
			color: "default"
		}))) : /* @__PURE__ */ __dil.jsx("box", {
			key: item,
			width: 4,
			height: 4,
			radius: "full",
			background: item < completedDots ? "surface-inverted" : "border-subtle"
		})]))))), /* @__PURE__ */ __dil.jsx("title", {
			size: isDesktop ? "xl" : "lg",
			weight: "medium",
			textAlign: "end"
		}, state.destination_code)), /* @__PURE__ */ __dil.jsx("row", {
			align: "start",
			gap: 4
		}, /* @__PURE__ */ __dil.jsx("col", {
			gap: .5,
			flex: 1,
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "sm",
			weight: "medium",
			maxLines: 2
		}, state.origin_city), /* @__PURE__ */ __dil.jsx("row", {
			align: "baseline",
			gap: 1,
			wrap: "wrap"
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: isDesktop ? "md" : "sm",
			weight: "medium",
			color: state.departure_time_color
		}, state.departure_time_label), state.departure_scheduled_time_label !== "" && /* @__PURE__ */ __dil.jsx("text", {
			size: "xs",
			color: "secondary",
			lineThrough: true
		}, state.departure_scheduled_time_label))), /* @__PURE__ */ __dil.jsx("col", {
			gap: .5,
			align: "end",
			flex: 1,
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "sm",
			weight: "medium",
			textAlign: "end",
			maxLines: 2
		}, state.destination_city), /* @__PURE__ */ __dil.jsx("row", {
			align: "baseline",
			gap: 1,
			wrap: "wrap",
			justify: "end"
		}, state.arrival_scheduled_time_label !== "" && /* @__PURE__ */ __dil.jsx("text", {
			size: "xs",
			color: "secondary",
			textAlign: "end",
			lineThrough: true
		}, state.arrival_scheduled_time_label), /* @__PURE__ */ __dil.jsx("text", {
			size: isDesktop ? "md" : "sm",
			weight: "medium",
			textAlign: "end",
			color: state.arrival_time_color
		}, state.arrival_time_label))))))), /* @__PURE__ */ __dil.jsx("divider", {
			flush: true,
			color: "subtle"
		}), /* @__PURE__ */ __dil.jsx("box", { padding: {
			x: 5,
			y: 3
		} }, /* @__PURE__ */ __dil.jsx("row", {
			align: "baseline",
			gap: .5,
			wrap: "wrap"
		}, state.summary_lead !== "" && /* @__PURE__ */ __dil.jsx("text", {
			size: "sm",
			weight: "medium"
		}, state.summary_lead), state.summary_emphasis !== "" && /* @__PURE__ */ __dil.jsx("text", {
			size: "sm",
			weight: "medium",
			color: state.summary_emphasis_color
		}, state.summary_emphasis), state.summary_trail !== "" && /* @__PURE__ */ __dil.jsx("text", {
			size: "sm",
			weight: "medium"
		}, state.summary_trail))), /* @__PURE__ */ __dil.jsx("divider", {
			flush: true,
			color: "subtle"
		}), /* @__PURE__ */ __dil.jsx("box", { padding: {
			x: 5,
			y: 4
		} }, /* @__PURE__ */ __dil.jsx("grid", {
			columns: isDesktop ? 2 : 1,
			gap: isDesktop ? 8 : 4
		}, /* @__PURE__ */ __dil.jsx("grid-item", null, /* @__PURE__ */ __dil.jsx(FlightDetails, {
			details: departureDetails,
			isDesktop,
			arrival: false
		})), /* @__PURE__ */ __dil.jsx("grid-item", null, /* @__PURE__ */ __dil.jsx(FlightDetails, {
			details: arrivalDetails,
			isDesktop,
			arrival: true
		})))), /* @__PURE__ */ __dil.jsx("divider", {
			flush: true,
			color: "subtle"
		}), /* @__PURE__ */ __dil.jsx("box", { padding: {
			x: 5,
			y: 4
		} }, /* @__PURE__ */ __dil.jsx("text", {
			size: "xs",
			color: "secondary"
		}, state.attribution_label))));
	}
	function FlightTracker(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("FlightTracker", props);
		if (state?.is_loading === true || !state && !resolutionComplete) return /* @__PURE__ */ __dil.jsx("loading-block", {
			width: "100%",
			height: 376
		});
		if (!state) return props.fallback ?? null;
		return /* @__PURE__ */ __dil.jsx(ResolvedFlightTracker, { state });
	}

//#endregion
return FlightTracker;
})();
return __dilDefaultExport;
})()))\`,FollowUp:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentFollowUp(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "FollowUp",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "FollowUp", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentFollowUp, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region FollowUp.dil.tsx
	function FollowUp({ query, value }) {
		return /* @__PURE__ */ __dil.jsx("pressable", {
			inline: true,
			onClick: () => GenUI.issueNewTurn(query ?? value)
		}, /* @__PURE__ */ __dil.jsx("text", { inline: true }, /* @__PURE__ */ __dil.jsx("icon", {
			inline: true,
			name: "arrow-curved"
		}), " ", /* @__PURE__ */ __dil.jsx("text", {
			inline: true,
			underline: "dotted"
		}, value)));
	}

//#endregion
return FollowUp;
})();
return __dilDefaultExport;
})()))\`,FollowUpActionBar:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentFollowUpActionBar(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "FollowUpActionBar",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "FollowUpActionBar", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentFollowUpActionBar, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region internal/FollowUpActionBar.dil.tsx
	function FollowUpFooter({ actions, onAction }) {
		const isSm = DIL.useBreakpoint("sm");
		const isXs = DIL.useBreakpoint("xs");
		const visibleActions = actions.slice(0, isSm ? 3 : isXs ? 2 : 1);
		return actions.length ? /* @__PURE__ */ __dil.jsx("box", { width: "100%" }, /* @__PURE__ */ __dil.jsx("box", { padding: {
			x: 5,
			y: 3
		} }, /* @__PURE__ */ __dil.jsx("divider", null)), /* @__PURE__ */ __dil.jsx("box", { padding: {
			bottom: 5,
			left: 5,
			right: 5
		} }, /* @__PURE__ */ __dil.jsx("box", {
			background: "surface-secondary",
			border: {
				color: "alpha-12",
				size: 1
			},
			padding: {
				x: .5,
				y: .5
			},
			radius: "lg"
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			ariaLabel: "Suggested follow-ups",
			gap: 0,
			role: "group",
			width: "100%"
		}, visibleActions.map((action, index) => /* @__PURE__ */ __dil.jsx("box", {
			align: "center",
			direction: "row",
			flex: 1,
			key: \\\`\\\${action.query}:\\\${index}\\\`,
			minWidth: 0
		}, index > 0 ? /* @__PURE__ */ __dil.jsx("box", {
			ariaHidden: true,
			background: "alpha-10",
			height: 24,
			width: 1
		}) : null, /* @__PURE__ */ __dil.jsx("box", {
			align: "center",
			flex: 1,
			justify: "center",
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("button", {
			block: true,
			color: "secondary",
			onClick: () => onAction(action),
			size: "sm",
			variant: "ghost"
		}, action.icon ? /* @__PURE__ */ __dil.jsx("icon", { name: action.icon }) : null, " ", action.label)))))))) : null;
	}

//#endregion
//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region FollowUpActionBar.dil.tsx
	function FollowUpActionBar(props) {
		const componentKey = DIL.useAppData((appData) => typeof props.__resolutionId === "string" && appData.opGenui?.componentResults?.[props.__resolutionId] != null ? props.__resolutionId : componentDataKey("FollowUpActionBar", props));
		const { resolutionComplete, state } = useResolvedComponentResult("FollowUpActionBar", {
			...props,
			__resolutionId: componentKey
		});
		if (!state) return resolutionComplete ? props.fallback ?? null : /* @__PURE__ */ __dil.jsx("loading-block", {
			width: "100%",
			height: 64
		});
		if (!state.actions.length) return null;
		const contentReferenceType = state.content_reference_type ?? state.contentReferenceType;
		return state.presentation === "wrapped_pills" ? /* @__PURE__ */ __dil.jsx("col", {
			role: "group",
			ariaLabel: "Suggested follow-ups",
			gap: 2,
			width: "100%",
			align: "start"
		}, state.actions.map((action, index) => /* @__PURE__ */ __dil.jsx("box", {
			key: action.query + "|" + action.label + "|" + index,
			width: "fit-content",
			height: 40,
			flex: "0 1 auto",
			...GenUI.logEvent ? {
				onVisibleWhen: action.onboarding_action_id != null && action.onboarding_action_id !== "",
				onVisible: () => GenUI.logEvent?.({
					event_name: "chatgpt_conversational_onboarding_action_shown",
					statsig_event_name: "chatgpt_conversational_onboarding_action_shown",
					event_data: {
						renderer: action.onboarding_renderer,
						level: action.onboarding_level,
						use_case: action.onboarding_use_case,
						action_id: action.onboarding_action_id,
						source: action.source
					}
				})
			} : {}
		}, /* @__PURE__ */ __dil.jsx("pressable", {
			width: "fit-content",
			height: 40,
			flex: "0 1 auto",
			padding: {
				top: 2,
				right: 4,
				bottom: 2,
				left: 2
			},
			radius: "full",
			border: {
				size: 1,
				color: "default"
			},
			background: "surface",
			clip: true,
			ariaLabel: action.label,
			onClick: () => GenUI.dispatchAction({
				handler: "client",
				type: action.onboarding_client_action === "open_image_generation_composer" || action.onboarding_client_action === "start_voice_mode" ? action.onboarding_client_action : "issue_new_turn",
				loadingBehavior: action.onboarding_action_id || action.source === "image_gen_follow_up_dil_buttons" ? "none" : "auto",
				payload: {
					...action.onboarding_client_action === "start_voice_mode" ? {
						component_key: componentKey,
						action_index: index
					} : {},
					query: action.query,
					append_to_current_leaf: true,
					event_source: action.onboarding_client_action !== void 0 ? "conversational_onboarding" : null,
					event_name: action.onboarding_action_id ? "chatgpt_conversational_onboarding_action_clicked" : null,
					statsig_event_name: action.onboarding_action_id ? "chatgpt_conversational_onboarding_action_clicked" : null,
					event_data: {
						renderer: action.onboarding_renderer,
						level: action.onboarding_level,
						use_case: action.onboarding_use_case,
						action_id: action.onboarding_action_id,
						source: action.source
					},
					message_metadata: action.source ? {
						followups_v2_followup_clicked: true,
						followups_v2_followup_source: action.source
					} : {}
				}
			})
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 2,
			width: "100%",
			height: "100%"
		}, action.icon_url ? /* @__PURE__ */ __dil.jsx("box", {
			size: 24,
			flex: "0 0 auto"
		}, /* @__PURE__ */ __dil.jsx("image", {
			src: action.icon_url,
			alt: "",
			fit: "contain",
			width: 24,
			height: 24,
			hideOnFailure: true
		})) : null, /* @__PURE__ */ __dil.jsx("text", {
			size: "sm",
			weight: "normal",
			color: "primary",
			maxLines: 1
		}, action.label)))))) : /* @__PURE__ */ __dil.jsx(FollowUpFooter, {
			actions: state.actions,
			onAction: (action) => GenUI.dispatchAction({
				handler: "client",
				type: "issue_new_turn",
				payload: {
					query: action.query,
					prompt: action.query,
					event_name: "chatgpt_widget_action",
					event_data: {
						widget_affordance: "OPTION",
						widget_action_target: "follow_up",
						widget_action_value: action.label,
						surface_location: "RESPONSE",
						...contentReferenceType ? { content_reference_type: contentReferenceType } : {}
					}
				}
			})
		});
	}

//#endregion
return FollowUpActionBar;
})();
return __dilDefaultExport;
})()))\`,GenImage:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentGenImage(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "GenImage",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "GenImage", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentGenImage, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function hasImageSource(image) {
		return Boolean(image?.content_url || image?.thumbnail_url);
	}
	function normalizedAspectRatio(value) {
		return value?.replace(":", " / ");
	}
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region GenImage.dil.tsx
/** Mirror v1: only pixel max-widths supply a non-shrinking preferred width. */
	function hasPreferredPixelWidth(value) {
		if (typeof value === "number") return Number.isFinite(value) && value >= 0;
		return typeof value === "string" && /^\\\\d+(?:\\\\.\\\\d+)?(?:px)?$/.test(value.trim());
	}
	/** Mirror the v1 intrinsic frame while image resolution is still pending. */
	function unresolvedAspectWidth(maxHeight, aspectRatio) {
		return \\\`calc(\\\${typeof maxHeight === "number" ? \\\`\\\${maxHeight}px\\\` : /^\\\\d+(?:\\\\.\\\\d+)?$/.test(maxHeight.trim()) ? \\\`\\\${maxHeight.trim()}px\\\` : maxHeight.trim()} * \\\${normalizedAspectRatio(aspectRatio)})\\\`;
	}
	function GenImage(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("GenImage", props);
		const isLoading = state?.is_loading ?? !resolutionComplete;
		const image = state?.images?.[0];
		const hasImage = hasImageSource(image);
		if (!isLoading && !hasImage && (!props.fallback || state == null)) return /* @__PURE__ */ __dil.jsx("box", { height: 0 });
		const frameMaxWidth = state?.frame_max_width ?? props.maxWidth;
		const frameAspectRatio = state?.frame_aspect_ratio ?? props.aspectRatio;
		const presentation = state?.presentation;
		const hasAuthoredDimensions = props.width != null || props.height != null;
		const unresolvedSize = isLoading && state == null && props.aspectRatio == null && !hasAuthoredDimensions ? props.maxHeight ?? 220 : void 0;
		const unresolvedWidth = isLoading && state == null && props.aspectRatio != null ? unresolvedAspectWidth(props.maxHeight ?? 220, props.aspectRatio) : unresolvedSize;
		const imageElement = /* @__PURE__ */ __dil.jsx("search-image", {
			alt: image?.title ?? "",
			aspectRatio: normalizedAspectRatio(frameAspectRatio),
			fallback: props.fallback,
			fit: state?.image_fit ?? "cover",
			height: "100%",
			hideOnFailure: true,
			radius: "none",
			searchImage: image,
			width: "100%"
		});
		return /* @__PURE__ */ __dil.jsx("box", {
			ariaLabel: isLoading ? "Loading image" : void 0,
			aspectRatio: normalizedAspectRatio(frameAspectRatio),
			background: "surface-tertiary",
			border: (presentation?.frame ?? props.frame ?? true) && !isLoading && hasImage ? {
				color: "subtle",
				size: 1
			} : void 0,
			clip: true,
			flex: hasPreferredPixelWidth(frameMaxWidth) ? "0 0 auto" : void 0,
			height: state?.frame_height ?? props.height ?? (props.width != null || frameAspectRatio != null ? void 0 : unresolvedSize ?? "100%"),
			maxHeight: state?.max_height !== void 0 ? state.max_height ?? void 0 : props.maxHeight ?? (hasAuthoredDimensions ? void 0 : unresolvedSize),
			maxWidth: frameMaxWidth,
			minHeight: presentation?.min_height ?? props.minHeight,
			minWidth: presentation?.min_width ?? props.minWidth ?? 0,
			radius: presentation?.radius ?? props.radius ?? "2xl",
			role: isLoading ? "status" : void 0,
			width: state?.frame_width ?? props.width ?? unresolvedWidth ?? "100%"
		}, !isLoading && !hasImage ? props.fallback : props.onClickAction === null ? imageElement : /* @__PURE__ */ __dil.jsx("pressable", {
			ariaLabel: image?.title ? \\\`Open \\\${image.title}\\\` : "Open image",
			background: "transparent",
			disabled: isLoading,
			height: "100%",
			onClick: () => props.onClickAction ? GenUI.dispatchAction(props.onClickAction) : state?.default_on_click_action ? GenUI.dispatchAction(state.default_on_click_action) : GenUI.openImageLightbox(state?.images ?? [], 0),
			radius: "none",
			width: "100%"
		}, imageElement));
	}

//#endregion
return GenImage;
})();
return __dilDefaultExport;
})()))\`,LearningSpeakCard:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentLearningSpeakCard(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "LearningSpeakCard",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "LearningSpeakCard", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentLearningSpeakCard, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region SpeechSynthesizer.dil.tsx
	const unavailable = {
		available: false,
		status: "idle",
		error: null,
		play: () => {},
		stop: () => {}
	};
	const { useSpeechSynthesizer = () => unavailable } = DIL;
	function SpeechSynthesizer({ controls, disabled = false, logging, ...source }) {
		const speech = useSpeechSynthesizer(source, logging);
		DIL.useEffect(() => {
			if (disabled) speech.stop();
		}, [disabled, speech.stop]);
		const play = () => {
			if (!disabled) speech.play();
		};
		const stop = () => {
			if (!disabled) speech.stop();
		};
		if (controls) return controls({
			...speech,
			play,
			stop
		});
		if (!speech.available) return /* @__PURE__ */ __dil.jsx("text", null, "Audio unavailable");
		return speech.status === "idle" ? /* @__PURE__ */ __dil.jsx("button", {
			disabled,
			onClick: play
		}, "Play") : /* @__PURE__ */ __dil.jsx("button", { onClick: stop }, "Stop");
	}

//#endregion
//#region learning/primitives/LearningExercisePager.dil.tsx
	function LearningExercisePager({ exercises, previousLabel, nextLabel, nextButtonLabel, progressLabels, newTurnQuery, renderExercise, renderFooter, getResponse, isExerciseComplete = (_, selectedIds$1) => selectedIds$1.length > 0 }) {
		const instanceId = DIL.useId();
		const [viewState, setViewState] = DIL.useState({
			currentId: null,
			answers: {}
		});
		const currentViewState = DIL.useRef(viewState);
		currentViewState.current = viewState;
		const updateViewState = (next) => {
			currentViewState.current = next;
			setViewState(next);
		};
		const index = Math.max(0, exercises.findIndex((exercise$1) => exercise$1.id === viewState.currentId));
		const exercise = exercises[index];
		if (!exercise) return null;
		const answer = viewState.answers[exercise.id];
		const selectedIds = answer?.revision === exercise.revision ? answer.selectedIds : [];
		const canAdvance = index < exercises.length - 1 && isExerciseComplete(exercise.data, selectedIds);
		const navigate = (nextIndex) => {
			if (nextIndex > index && !canAdvance) return;
			const next = exercises[nextIndex];
			if (next) updateViewState({
				...currentViewState.current,
				currentId: next.id
			});
		};
		const navigation = exercises.length > 1 ? /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 1.25,
			flex: "0 0 auto"
		}, /* @__PURE__ */ __dil.jsx("pressable", {
			size: 36,
			radius: "full",
			background: "transparent",
			ariaLabel: previousLabel,
			disabled: index === 0,
			onClick: () => navigate(index - 1)
		}, /* @__PURE__ */ __dil.jsx("box", {
			height: "100%",
			align: "center",
			justify: "center",
			ariaHidden: true
		}, /* @__PURE__ */ __dil.jsx("icon", {
			name: "chevron-left",
			size: "xl",
			color: index === 0 ? "alpha-35" : "secondary"
		}))), /* @__PURE__ */ __dil.jsx("box", {
			role: "status",
			minWidth: 48,
			align: "center",
			justify: "center"
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "sm",
			color: "secondary"
		}, progressLabels[index])), /* @__PURE__ */ __dil.jsx("pressable", {
			size: 36,
			radius: "full",
			background: "transparent",
			ariaLabel: nextLabel,
			disabled: !canAdvance,
			onClick: () => navigate(index + 1)
		}, /* @__PURE__ */ __dil.jsx("box", {
			height: "100%",
			align: "center",
			justify: "center",
			ariaHidden: true
		}, /* @__PURE__ */ __dil.jsx("icon", {
			name: "chevron-right",
			size: "xl",
			color: canAdvance ? "secondary" : "alpha-35"
		})))) : null;
		return /* @__PURE__ */ __dil.jsx("box", {
			key: \\\`\\\${instanceId}:\\\${exercise.id}:\\\${exercise.revision}\\\`,
			width: "100vw",
			maxWidth: "100%"
		}, renderExercise(exercise.data, {
			selectedIds,
			setSelectedIds: (update) => {
				const current = currentViewState.current;
				const previous = current.answers[exercise.id];
				const ids = previous?.revision === exercise.revision ? previous.selectedIds : [];
				const nextIds = update(ids);
				const answers = {
					...current.answers,
					[exercise.id]: {
						revision: exercise.revision,
						selectedIds: nextIds
					}
				};
				updateViewState({
					...current,
					answers
				});
				if (newTurnQuery && index === exercises.length - 1 && !isExerciseComplete(exercise.data, ids) && isExerciseComplete(exercise.data, nextIds)) {
					const responses = exercises.flatMap((item, itemIndex) => {
						const exerciseAnswer = answers[item.id];
						if (exerciseAnswer?.revision !== item.revision || !exerciseAnswer.selectedIds.length) return [];
						const response = getResponse(item.data, exerciseAnswer.selectedIds);
						return [\\\`\\\${itemIndex + 1}. \\\${response.question}\\\\n→ \\\${response.answer}\\\`];
					});
					GenUI.issueNewTurn([newTurnQuery, ...responses].join("\\\\n\\\\n"), {
						append_to_current_leaf: true,
						followup_metadata: {}
					});
				}
			}
		}, navigation, renderFooter ? renderFooter(canAdvance ? () => navigate(index + 1) : null) : exercises.length > 1 ? /* @__PURE__ */ __dil.jsx("row", {
			justify: "end",
			minHeight: "40px"
		}, /* @__PURE__ */ __dil.jsx("button", {
			size: "lg",
			color: "primary",
			variant: canAdvance ? "solid" : "outline",
			disabled: !canAdvance,
			onClick: () => navigate(index + 1)
		}, nextButtonLabel ?? nextLabel)) : null));
	}

//#endregion
//#region learning/speaking.dil.ts
	const REPEAT_ATTEMPT_TIMEOUT_MS = 1e4;
	function wordKey(text) {
		return text.toLowerCase().normalize("NFD").replace(/(\\\\p{Script=Latin})\\\\p{M}+/gu, "$1").replace(/[^\\\\p{L}\\\\p{N}\\\\p{M}]/gu, "");
	}
	function hasSpeakingSegmenter() {
		return typeof Intl !== "undefined" && typeof Intl.Segmenter === "function";
	}
	/** Keep display spans intact while comparing normalized, whole words. */
	function speakingParts(text, language) {
		if (!hasSpeakingSegmenter()) return [];
		let segmenter;
		try {
			segmenter = new Intl.Segmenter(language || void 0, { granularity: "word" });
		} catch {
			segmenter = new Intl.Segmenter(void 0, { granularity: "word" });
		}
		const parts = Array.from(segmenter.segment(text), (part) => ({
			text: part.segment,
			word: part.isWordLike ? wordKey(part.segment) || null : null
		}));
		const displayParts = [];
		for (const part of parts) {
			const previous = displayParts[displayParts.length - 1];
			const opening = part.word && previous && !previous.word ? previous.text.match(/[¿¡]+$/u)?.[0] : void 0;
			if (opening) {
				previous.text = previous.text.slice(0, -opening.length);
				if (!previous.text) displayParts.pop();
				displayParts.push({
					text: opening + part.text,
					word: part.word
				});
				continue;
			}
			const punctuation = !part.word && previous?.word ? part.text.match(/^(?:(?![¿¡])\\\\p{P})+/u)?.[0] : void 0;
			if (punctuation) {
				previous.text += punctuation;
				const rest = part.text.slice(punctuation.length);
				if (rest) displayParts.push({
					text: rest,
					word: null
				});
			} else if (!part.word && previous && !previous.word) previous.text += part.text;
			else displayParts.push(part);
		}
		return displayParts;
	}
	/** Every transcript is a replacement, never an additional batch of words. */
	function matchSpeakingWords(parts, transcript, language) {
		const counts = /* @__PURE__ */ new Map();
		for (const { word } of speakingParts(transcript, language)) if (word) counts.set(word, (counts.get(word) ?? 0) + 1);
		let remaining = 0;
		return {
			matched: parts.map(({ word }) => {
				if (!word) return false;
				const count = counts.get(word) ?? 0;
				if (count > 0) {
					counts.set(word, count - 1);
					return true;
				}
				remaining++;
				return false;
			}),
			complete: parts.some((part) => part.word !== null) && remaining === 0
		};
	}
	/** New occurrences count as activity even when they are not target words. */
	function hasNewSpeakingWords(previousTranscript, transcript, language) {
		if (transcript === previousTranscript) return false;
		const parts = speakingParts(transcript, language);
		return parts.some((part) => part.word !== null) && !matchSpeakingWords(parts, previousTranscript, language).complete;
	}

//#endregion
//#region learning/cards/LearningSpeakCard.dil.tsx
	const { useDictation = () => void 0 } = DIL;
	const SUCCESS = "spoken";
	const CARD_WIDTH = 399;
	const ACTION_HEIGHT = 48;
	const HEADER_CONTROL_SIZE = 40;
	const STOP_CONTROL_SIZE = 32;
	function LearningSpeakCard$1(props) {
		return /* @__PURE__ */ __dil.jsx(LearningExercisePager, {
			...props,
			newTurnQuery: null,
			isExerciseComplete: (_, ids) => ids.includes(SUCCESS),
			getResponse: (data) => ({
				question: data.text,
				answer: data.text
			}),
			renderFooter: (onNext) => onNext ? /* @__PURE__ */ __dil.jsx("pressable", {
				height: ACTION_HEIGHT,
				flex: "0 0 auto",
				padding: { x: 4 },
				radius: "full",
				background: "surface-inverted",
				ariaLabel: props.nextButtonLabel ?? props.nextLabel,
				onClick: onNext
			}, /* @__PURE__ */ __dil.jsx("row", {
				height: "100%",
				align: "center",
				justify: "center"
			}, /* @__PURE__ */ __dil.jsx("text", {
				size: "md",
				weight: "medium",
				color: {
					light: "white",
					dark: "black"
				}
			}, props.nextButtonLabel ?? props.nextLabel))) : null,
			renderExercise: (data, selection, navigation, footer) => /* @__PURE__ */ __dil.jsx(SpeakExercise, {
				...data,
				selection,
				navigation,
				footer
			})
		});
	}
	function SpeakExercise({ text, language, mode = "repeat", sourceText, labels, selection, navigation, footer }) {
		const dictation = useDictation(language ? { language } : void 0);
		const parts = DIL.useMemo(() => {
			const segmented = speakingParts(text, language);
			return segmented.length ? segmented : [{
				text,
				word: text
			}];
		}, [text, language]);
		const attempt = DIL.useRef({
			phase: "idle",
			lastActivityAt: null,
			transcript: "",
			matched: [],
			error: false
		});
		const [, refresh] = DIL.useState(0);
		const update = () => refresh((value) => value + 1);
		const succeeded = selection.selectedIds.includes(SUCCESS);
		const recall = mode === "recall";
		const active = !succeeded && attempt.current.phase === "active";
		const now = DIL.useNow(active && dictation?.status !== "starting");
		const transcript = dictation?.transcript ?? "";
		const liveMatch = DIL.useMemo(() => matchSpeakingWords(parts, transcript, language), [
			parts,
			transcript,
			language
		]);
		const unavailable$1 = !dictation || dictation.error?.code === "integration_error" || !hasSpeakingSegmenter();
		function startAttempt(stopPlayback) {
			if (!dictation || unavailable$1 || attempt.current.phase === "active") return;
			stopPlayback();
			dictation.cancel();
			selection.setSelectedIds(() => []);
			attempt.current = {
				phase: "active",
				lastActivityAt: null,
				transcript: "",
				matched: [],
				error: false
			};
			dictation.start();
			update();
		}
		function finish(success, error = false) {
			if (attempt.current.phase !== "active") return;
			attempt.current = {
				...attempt.current,
				phase: success ? "success" : "failure",
				error
			};
			dictation?.stop();
			if (success) selection.setSelectedIds(() => [SUCCESS]);
			update();
		}
		DIL.useEffect(() => {
			if (succeeded) {
				if (attempt.current.phase === "active") {
					attempt.current = {
						...attempt.current,
						phase: "success"
					};
					dictation?.cancel();
				}
				return;
			}
			if (!active || !dictation) return;
			if (dictation.status === "error" || dictation.status === "idle") {
				finish(false, true);
				return;
			}
			if (dictation.status === "starting") return;
			const lastActivityAt = attempt.current.lastActivityAt ?? now;
			if (now - lastActivityAt >= REPEAT_ATTEMPT_TIMEOUT_MS) {
				finish(false);
				return;
			}
			attempt.current = {
				...attempt.current,
				lastActivityAt: hasNewSpeakingWords(attempt.current.transcript, dictation.transcript, language) ? now : lastActivityAt,
				transcript: dictation.transcript,
				matched: liveMatch.matched
			};
			if (liveMatch.complete) finish(true);
			else if (dictation.status === "done") finish(false);
		});
		const cancel = dictation?.cancel;
		DIL.useEffect(() => () => cancel?.(), [cancel]);
		const failed = attempt.current.phase === "failure";
		const showPlayback = !recall || succeeded || failed && !attempt.current.error;
		const expired = attempt.current.lastActivityAt !== null && now - attempt.current.lastActivityAt >= REPEAT_ATTEMPT_TIMEOUT_MS;
		const matched = active && !expired ? liveMatch.matched : attempt.current.matched;
		const showMissingWords = failed && !succeeded && !attempt.current.error;
		const wordColors = parts.map(({ word }, index) => {
			if (!word || succeeded) return "primary";
			if (matched[index]) return active ? "info" : "primary";
			return showMissingWords ? "danger" : "primary";
		});
		const message = succeeded ? null : unavailable$1 ? labels.unavailable : failed && attempt.current.error ? labels.error : null;
		return /* @__PURE__ */ __dil.jsx(SpeechSynthesizer, {
			text,
			language: language ?? void 0,
			source: "learning_speak_card",
			disabled: active || !showPlayback,
			controls: (speech) => /* @__PURE__ */ __dil.jsx("col", {
				width: CARD_WIDTH,
				maxWidth: "100%",
				padding: 5,
				gap: 2,
				radius: "4xl",
				background: "surface-elevated",
				border: {
					size: .5,
					color: "default"
				},
				role: "group",
				ariaLabel: labels.title,
				disableAutoSpacing: true
			}, /* @__PURE__ */ __dil.jsx("row", {
				align: "center",
				justify: "between",
				minHeight: HEADER_CONTROL_SIZE,
				gap: 2,
				wrap: "wrap"
			}, /* @__PURE__ */ __dil.jsx("row", {
				align: "center",
				gap: 1.5,
				flex: "1 1 auto"
			}, /* @__PURE__ */ __dil.jsx("box", { ariaHidden: true }, /* @__PURE__ */ __dil.jsx("icon", {
				name: "voice",
				size: "lg",
				color: "secondary"
			})), /* @__PURE__ */ __dil.jsx("text", {
				size: "md",
				color: "secondary",
				weight: "medium"
			}, labels.title)), /* @__PURE__ */ __dil.jsx(__dil.Fragment, null, navigation), showPlayback ? /* @__PURE__ */ __dil.jsx("pressable", {
				size: HEADER_CONTROL_SIZE,
				radius: "full",
				background: "transparent",
				ariaLabel: !speech.available ? labels.playbackUnavailable : speech.status === "idle" ? labels.play : labels.stopPlayback,
				disabled: active || !speech.available,
				onClick: speech.status === "idle" ? speech.play : speech.stop
			}, /* @__PURE__ */ __dil.jsx("box", {
				height: "100%",
				align: "center",
				justify: "center",
				ariaHidden: true
			}, /* @__PURE__ */ __dil.jsx("icon", {
				name: speech.status === "idle" ? "sound-on-read-out-loud-speaker" : "stop",
				size: "xl",
				color: "secondary"
			}))) : null), recall ? /* @__PURE__ */ __dil.jsx("col", {
				gap: 4,
				padding: { bottom: 4 }
			}, /* @__PURE__ */ __dil.jsx("col", { gap: 1.5 }, /* @__PURE__ */ __dil.jsx("text", {
				size: "sm",
				color: "secondary"
			}, labels.recallInstruction), /* @__PURE__ */ __dil.jsx("text", {
				size: "xl",
				preserveWhitespace: true
			}, sourceText)), /* @__PURE__ */ __dil.jsx(RecallPhrase, {
				parts,
				matched,
				wordColors,
				succeeded,
				failed: showMissingWords,
				hiddenWord: labels.hiddenWord ?? ""
			})) : /* @__PURE__ */ __dil.jsx("box", { padding: { bottom: 4 } }, /* @__PURE__ */ __dil.jsx("text", {
				size: "xl",
				preserveWhitespace: true
			}, parts.map((part, index) => part.word ? /* @__PURE__ */ __dil.jsx("text", {
				key: index,
				inline: true,
				color: wordColors[index]
			}, part.text) : part.text))), /* @__PURE__ */ __dil.jsx("col", {
				role: "status",
				gap: 2
			}, succeeded ? /* @__PURE__ */ __dil.jsx("row", {
				align: "center",
				gap: 2
			}, /* @__PURE__ */ __dil.jsx("row", {
				height: ACTION_HEIGHT,
				flex: "1 1 auto",
				padding: { x: 4 },
				radius: "full",
				background: "green-500",
				align: "center",
				justify: "between"
			}, /* @__PURE__ */ __dil.jsx("text", {
				color: "white",
				weight: "medium"
			}, labels.success), /* @__PURE__ */ __dil.jsx("box", { ariaHidden: true }, /* @__PURE__ */ __dil.jsx("icon", {
				name: "check-md",
				color: "white",
				size: "xl"
			}))), /* @__PURE__ */ __dil.jsx("pressable", {
				size: ACTION_HEIGHT,
				flex: "0 0 auto",
				radius: "full",
				background: "surface-tertiary",
				ariaLabel: labels.retry,
				disabled: unavailable$1,
				onClick: () => startAttempt(speech.stop)
			}, /* @__PURE__ */ __dil.jsx("box", {
				height: "100%",
				align: "center",
				justify: "center",
				ariaHidden: true
			}, /* @__PURE__ */ __dil.jsx("icon", {
				name: "reload",
				color: "primary",
				size: "xl"
			}))), /* @__PURE__ */ __dil.jsx(__dil.Fragment, null, footer)) : active ? /* @__PURE__ */ __dil.jsx("row", {
				height: ACTION_HEIGHT,
				padding: {
					left: 4,
					right: 2
				},
				radius: "full",
				background: "surface-tertiary",
				align: "center",
				justify: "between"
			}, /* @__PURE__ */ __dil.jsx("shimmer-text", { weight: "medium" }, dictation?.status === "starting" ? labels.starting : labels.listening), /* @__PURE__ */ __dil.jsx("pressable", {
				size: STOP_CONTROL_SIZE,
				radius: "full",
				background: "surface-inverted",
				ariaLabel: labels.stop,
				onClick: () => {
					const withinDeadline = attempt.current.lastActivityAt !== null && now - attempt.current.lastActivityAt < REPEAT_ATTEMPT_TIMEOUT_MS;
					if (withinDeadline) attempt.current = {
						...attempt.current,
						matched: liveMatch.matched
					};
					finish(withinDeadline && liveMatch.complete);
				}
			}, /* @__PURE__ */ __dil.jsx("box", {
				height: "100%",
				align: "center",
				justify: "center",
				ariaHidden: true
			}, /* @__PURE__ */ __dil.jsx("icon", {
				name: "stop",
				color: {
					light: "white",
					dark: "black"
				},
				size: "lg"
			})))) : /* @__PURE__ */ __dil.jsx("pressable", {
				height: ACTION_HEIGHT,
				width: "100%",
				radius: "full",
				background: "surface-inverted",
				ariaLabel: failed ? labels.retry : labels.start,
				disabled: unavailable$1 || !parts.some((part) => part.word),
				onClick: () => startAttempt(speech.stop)
			}, /* @__PURE__ */ __dil.jsx("row", {
				height: "100%",
				align: "center",
				justify: "center",
				gap: 1.5
			}, /* @__PURE__ */ __dil.jsx("box", { ariaHidden: true }, /* @__PURE__ */ __dil.jsx("icon", {
				name: failed ? "reload" : "mic",
				color: {
					light: "white",
					dark: "black"
				}
			})), /* @__PURE__ */ __dil.jsx("text", {
				color: {
					light: "white",
					dark: "black"
				},
				weight: "medium"
			}, failed ? labels.retry : labels.start))), message ? /* @__PURE__ */ __dil.jsx("text", {
				size: "sm",
				color: "secondary"
			}, message) : null, speech.error ? /* @__PURE__ */ __dil.jsx("text", {
				size: "sm",
				color: "secondary"
			}, labels.playbackError) : null))
		});
	}
	function RecallPhrase({ parts, matched, wordColors, succeeded, failed, hiddenWord }) {
		const lines = [[]];
		parts.forEach((part, index) => {
			part.text.split(/\\\\r\\\\n|\\\\r|\\\\n/).forEach((text, line) => {
				if (line > 0) lines.push([]);
				if (text) lines[lines.length - 1].push({
					text,
					index
				});
			});
		});
		return /* @__PURE__ */ __dil.jsx("col", {
			gap: 1,
			padding: {
				x: 6,
				y: 4
			},
			radius: "full",
			background: succeeded || failed ? "surface-tertiary" : "transparent",
			border: succeeded || failed ? void 0 : {
				size: 1,
				color: "strong",
				style: "dashed"
			}
		}, lines.map((line, lineIndex) => /* @__PURE__ */ __dil.jsx("row", {
			key: lineIndex,
			wrap: "wrap",
			gap: 0,
			rowGap: 1,
			align: "center"
		}, line.length === 0 ? /* @__PURE__ */ __dil.jsx("box", { ariaHidden: true }, /* @__PURE__ */ __dil.jsx("text", {
			size: "xl",
			preserveWhitespace: true
		}, "\\\\xA0")) : line.map(({ text, index }) => parts[index].word && !succeeded && !failed && !matched[index] ? /* @__PURE__ */ __dil.jsx("box", {
			key: index,
			radius: "xs",
			background: "surface-tertiary",
			maxWidth: "100%",
			role: "img",
			ariaLabel: hiddenWord
		}, /* @__PURE__ */ __dil.jsx("box", { ariaHidden: true }, /* @__PURE__ */ __dil.jsx("text", {
			size: "xl",
			color: "transparent",
			preserveWhitespace: true
		}, text))) : /* @__PURE__ */ __dil.jsx("text", {
			key: index,
			size: "xl",
			inline: true,
			color: wordColors[index],
			preserveWhitespace: true
		}, text)))));
	}

//#endregion
//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) {
			if (result.status === "pending") return {
				status: "pending",
				resolutionComplete: false,
				state: void 0
			};
			return result.status === "resolved" ? {
				status: "resolved",
				resolutionComplete: true,
				state: result.state
			} : {
				status: "failed",
				resolutionComplete: true,
				state: void 0
			};
		}
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "pending") return true;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region LearningSpeakCard.dil.tsx
	function LearningSpeakCard(props) {
		const { state } = useResolvedComponentResult("LearningSpeakCard", props);
		if (state) return /* @__PURE__ */ __dil.jsx(LearningSpeakCard$1, state);
		if ("progressLabels" in props) return /* @__PURE__ */ __dil.jsx(LearningSpeakCard$1, props);
		return props.fallback ?? null;
	}

//#endregion
return LearningSpeakCard;
})();
return __dilDefaultExport;
})()))\`,LearningVizDil:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentLearningVizDil(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "LearningVizDil",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "LearningVizDil", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentLearningVizDil, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region learning-viz-dil/lottie-playback.dil.ts
	function positionAt(playback, now, duration) {
		const elapsed = playback.startedAt === null ? 0 : (now - playback.startedAt) / 1e3;
		return Math.min(duration, playback.position + Math.max(0, elapsed));
	}
	function useLottiePlayback(source, animated) {
		const duration = (Number(source.op) - Number(source.ip)) / Number(source.fr);
		const [playback, setPlayback] = DIL.useState(() => ({
			position: 0,
			startedAt: animated ? Date.now() : null
		}));
		const now = DIL.useNow(playback.startedAt !== null);
		const playing = playback.startedAt !== null && positionAt(playback, now, duration) < duration;
		DIL.useEffect(() => {
			if (playback.startedAt !== null && !playing) setPlayback({
				position: duration,
				startedAt: null
			});
		}, [
			playing,
			playback.startedAt,
			duration
		]);
		const currentTime = DIL.useMemo(() => positionAt(playback, Date.now(), duration), [source, playback]);
		const toggle = () => {
			const timestamp = Date.now();
			setPlayback((previous) => {
				const position = positionAt(previous, timestamp, duration);
				return previous.startedAt !== null && position < duration ? {
					position,
					startedAt: null
				} : {
					position: position === duration ? 0 : position,
					startedAt: timestamp
				};
			});
		};
		return {
			playing,
			currentTime,
			toggle
		};
	}

//#endregion
//#region learning-viz-dil/lottie-diagram.dil.tsx
	function LottieDiagram({ data }) {
		const { theme } = DIL.useTheme();
		const playback = useLottiePlayback(data.src[theme], data.animated);
		return /* @__PURE__ */ __dil.jsx("col", {
			width: "100vw",
			maxWidth: "100%",
			align: "center",
			gap: 0
		}, /* @__PURE__ */ __dil.jsx("box", {
			width: "100%",
			maxWidth: 480
		}, /* @__PURE__ */ __dil.jsx("lottie", {
			key: theme,
			src: data.src,
			alt: data.alt,
			width: "100%",
			aspectRatio: "8/5",
			currentTime: playback.currentTime,
			advanceRate: playback.playing ? 1 : 0,
			loop: false
		})), data.animated ? /* @__PURE__ */ __dil.jsx("row", {
			width: "100%",
			maxWidth: 480,
			align: "end",
			justify: "end",
			gap: 0
		}, /* @__PURE__ */ __dil.jsx("box", { padding: {
			bottom: 3,
			right: 3
		} }, /* @__PURE__ */ __dil.jsx("pressable", {
			onClick: playback.toggle,
			ariaLabel: playback.playing ? data.pause_label : data.play_label,
			size: 36,
			radius: "full",
			align: "center",
			justify: "center"
		}, /* @__PURE__ */ __dil.jsx("box", {
			key: theme,
			size: 24,
			ariaHidden: true
		}, /* @__PURE__ */ __dil.jsx("icon", {
			name: playback.playing ? "pause-sm" : "play-sm",
			size: "2xl",
			color: theme === "dark" ? "#ffffff" : "#0d0d0d"
		}))))) : null);
	}

//#endregion
//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region LearningVizDil.dil.tsx
	function LearningVizDil(props) {
		const { state, resolutionComplete } = useResolvedComponentResult("LearningVizDil", props);
		if (!state) return resolutionComplete ? props.fallback ?? null : /* @__PURE__ */ __dil.jsx("loading-block", {
			width: "100%",
			height: 300
		});
		return /* @__PURE__ */ __dil.jsx(LottieDiagram, { data: state.block_data });
	}

//#endregion
return LearningVizDil;
})();
return __dilDefaultExport;
})()))\`,LearningVocabCard:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentLearningVocabCard(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "LearningVocabCard",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "LearningVocabCard", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentLearningVocabCard, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region SpeechSynthesizer.dil.tsx
	const unavailable = {
		available: false,
		status: "idle",
		error: null,
		play: () => {},
		stop: () => {}
	};
	const { useSpeechSynthesizer = () => unavailable } = DIL;
	function SpeechSynthesizer({ controls, disabled = false, logging, ...source }) {
		const speech = useSpeechSynthesizer(source, logging);
		DIL.useEffect(() => {
			if (disabled) speech.stop();
		}, [disabled, speech.stop]);
		const play = () => {
			if (!disabled) speech.play();
		};
		const stop = () => {
			if (!disabled) speech.stop();
		};
		if (controls) return controls({
			...speech,
			play,
			stop
		});
		if (!speech.available) return /* @__PURE__ */ __dil.jsx("text", null, "Audio unavailable");
		return speech.status === "idle" ? /* @__PURE__ */ __dil.jsx("button", {
			disabled,
			onClick: play
		}, "Play") : /* @__PURE__ */ __dil.jsx("button", { onClick: stop }, "Stop");
	}

//#endregion
//#region learning/primitives/LearningCard.dil.tsx
	function LearningCard({ icon, title, directions, prompt, children, navigation, footer, divider = true }) {
		return /* @__PURE__ */ __dil.jsx("col", {
			width: "100%",
			maxWidth: "100%",
			padding: 5,
			gap: 2,
			radius: "4xl",
			background: "surface-elevated",
			border: {
				size: .5,
				color: "strong"
			},
			role: "group",
			ariaLabel: title,
			disableAutoSpacing: true
		}, /* @__PURE__ */ __dil.jsx("row", {
			minHeight: "40px",
			gap: 2,
			align: "center",
			justify: "between",
			wrap: "wrap"
		}, /* @__PURE__ */ __dil.jsx("row", {
			gap: 1.5,
			align: "center",
			flex: "1 1 auto",
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("box", {
			ariaHidden: true,
			flex: "0 0 auto"
		}, /* @__PURE__ */ __dil.jsx("icon", {
			name: icon,
			size: "xl",
			color: "secondary"
		})), /* @__PURE__ */ __dil.jsx("text", {
			size: "md",
			color: "secondary",
			weight: "medium"
		}, title)), /* @__PURE__ */ __dil.jsx(__dil.Fragment, null, navigation)), /* @__PURE__ */ __dil.jsx("col", { gap: 4 }, directions ? /* @__PURE__ */ __dil.jsx("text", { size: "md" }, directions) : null, /* @__PURE__ */ __dil.jsx(__dil.Fragment, null, prompt), divider ? /* @__PURE__ */ __dil.jsx("box", {
			height: "1px",
			background: "border",
			role: "separator"
		}) : null, /* @__PURE__ */ __dil.jsx(__dil.Fragment, null, children), /* @__PURE__ */ __dil.jsx(__dil.Fragment, null, footer)));
	}

//#endregion
//#region learning/primitives/LearningExercisePager.dil.tsx
	function LearningExercisePager({ exercises, previousLabel, nextLabel, nextButtonLabel, progressLabels, newTurnQuery, renderExercise, renderFooter, getResponse, isExerciseComplete = (_, selectedIds$1) => selectedIds$1.length > 0 }) {
		const instanceId = DIL.useId();
		const [viewState, setViewState] = DIL.useState({
			currentId: null,
			answers: {}
		});
		const currentViewState = DIL.useRef(viewState);
		currentViewState.current = viewState;
		const updateViewState = (next) => {
			currentViewState.current = next;
			setViewState(next);
		};
		const index = Math.max(0, exercises.findIndex((exercise$1) => exercise$1.id === viewState.currentId));
		const exercise = exercises[index];
		if (!exercise) return null;
		const answer = viewState.answers[exercise.id];
		const selectedIds = answer?.revision === exercise.revision ? answer.selectedIds : [];
		const canAdvance = index < exercises.length - 1 && isExerciseComplete(exercise.data, selectedIds);
		const navigate = (nextIndex) => {
			if (nextIndex > index && !canAdvance) return;
			const next = exercises[nextIndex];
			if (next) updateViewState({
				...currentViewState.current,
				currentId: next.id
			});
		};
		const navigation = exercises.length > 1 ? /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 1.25,
			flex: "0 0 auto"
		}, /* @__PURE__ */ __dil.jsx("pressable", {
			size: 36,
			radius: "full",
			background: "transparent",
			ariaLabel: previousLabel,
			disabled: index === 0,
			onClick: () => navigate(index - 1)
		}, /* @__PURE__ */ __dil.jsx("box", {
			height: "100%",
			align: "center",
			justify: "center",
			ariaHidden: true
		}, /* @__PURE__ */ __dil.jsx("icon", {
			name: "chevron-left",
			size: "xl",
			color: index === 0 ? "alpha-35" : "secondary"
		}))), /* @__PURE__ */ __dil.jsx("box", {
			role: "status",
			minWidth: 48,
			align: "center",
			justify: "center"
		}, /* @__PURE__ */ __dil.jsx("text", {
			size: "sm",
			color: "secondary"
		}, progressLabels[index])), /* @__PURE__ */ __dil.jsx("pressable", {
			size: 36,
			radius: "full",
			background: "transparent",
			ariaLabel: nextLabel,
			disabled: !canAdvance,
			onClick: () => navigate(index + 1)
		}, /* @__PURE__ */ __dil.jsx("box", {
			height: "100%",
			align: "center",
			justify: "center",
			ariaHidden: true
		}, /* @__PURE__ */ __dil.jsx("icon", {
			name: "chevron-right",
			size: "xl",
			color: canAdvance ? "secondary" : "alpha-35"
		})))) : null;
		return /* @__PURE__ */ __dil.jsx("box", {
			key: \\\`\\\${instanceId}:\\\${exercise.id}:\\\${exercise.revision}\\\`,
			width: "100vw",
			maxWidth: "100%"
		}, renderExercise(exercise.data, {
			selectedIds,
			setSelectedIds: (update) => {
				const current = currentViewState.current;
				const previous = current.answers[exercise.id];
				const ids = previous?.revision === exercise.revision ? previous.selectedIds : [];
				const nextIds = update(ids);
				const answers = {
					...current.answers,
					[exercise.id]: {
						revision: exercise.revision,
						selectedIds: nextIds
					}
				};
				updateViewState({
					...current,
					answers
				});
				if (newTurnQuery && index === exercises.length - 1 && !isExerciseComplete(exercise.data, ids) && isExerciseComplete(exercise.data, nextIds)) {
					const responses = exercises.flatMap((item, itemIndex) => {
						const exerciseAnswer = answers[item.id];
						if (exerciseAnswer?.revision !== item.revision || !exerciseAnswer.selectedIds.length) return [];
						const response = getResponse(item.data, exerciseAnswer.selectedIds);
						return [\\\`\\\${itemIndex + 1}. \\\${response.question}\\\\n→ \\\${response.answer}\\\`];
					});
					GenUI.issueNewTurn([newTurnQuery, ...responses].join("\\\\n\\\\n"), {
						append_to_current_leaf: true,
						followup_metadata: {}
					});
				}
			}
		}, navigation, renderFooter ? renderFooter(canAdvance ? () => navigate(index + 1) : null) : exercises.length > 1 ? /* @__PURE__ */ __dil.jsx("row", {
			justify: "end",
			minHeight: "40px"
		}, /* @__PURE__ */ __dil.jsx("button", {
			size: "lg",
			color: "primary",
			variant: canAdvance ? "solid" : "outline",
			disabled: !canAdvance,
			onClick: () => navigate(index + 1)
		}, nextButtonLabel ?? nextLabel)) : null));
	}

//#endregion
//#region learning/cards/LearningVocabCard.dil.tsx
	function LearningVocabCard$1(props) {
		return /* @__PURE__ */ __dil.jsx("box", {
			width: "100vw",
			maxWidth: "100%"
		}, /* @__PURE__ */ __dil.jsx(LearningExercisePager, {
			...props,
			newTurnQuery: null,
			isExerciseComplete: () => true,
			getResponse: (data) => ({
				question: data.text,
				answer: ""
			}),
			renderExercise: (data, _selection, navigation, footer) => /* @__PURE__ */ __dil.jsx(SpeechSynthesizer, {
				text: data.text,
				language: data.language,
				pronunciationHint: data.pronunciationHint ?? void 0,
				source: "learning_vocab_card",
				controls: (speech) => {
					const playButton = /* @__PURE__ */ __dil.jsx("button", {
						size: "xl",
						variant: "solid",
						color: "info",
						label: !speech.available ? data.audioLabels.unavailable : speech.status === "idle" ? data.audioLabels.play : data.audioLabels.stop,
						disabled: !speech.available,
						onClick: speech.status === "idle" ? speech.play : speech.stop
					}, /* @__PURE__ */ __dil.jsx("row", {
						gap: 1,
						align: "center"
					}, /* @__PURE__ */ __dil.jsx("icon", {
						name: speech.status === "idle" ? "sound-on-read-out-loud-speaker" : "stop",
						size: "lg"
					}), /* @__PURE__ */ __dil.jsx(__dil.Fragment, null, speech.status === "idle" ? data.audioLabels.play : data.audioLabels.stop)));
					return /* @__PURE__ */ __dil.jsx(LearningCard, {
						icon: "book",
						title: data.title,
						navigation: props.exercises.length > 1 ? navigation : playButton,
						footer,
						prompt: /* @__PURE__ */ __dil.jsx("row", {
							gap: 2,
							align: "center",
							justify: "between"
						}, /* @__PURE__ */ __dil.jsx("box", {
							flex: "1 1 auto",
							minWidth: 0
						}, /* @__PURE__ */ __dil.jsx("title", {
							size: "lg",
							weight: "normal"
						}, data.text)), props.exercises.length > 1 ? /* @__PURE__ */ __dil.jsx("box", { flex: "0 0 auto" }, playButton) : null)
					}, /* @__PURE__ */ __dil.jsx("col", { gap: 2 }, /* @__PURE__ */ __dil.jsx("text", { size: "md" }, data.translation), data.pronunciation ? /* @__PURE__ */ __dil.jsx("text", {
						size: "md",
						color: "secondary",
						italic: true
					}, data.pronunciation) : null, speech.error ? /* @__PURE__ */ __dil.jsx("box", { role: "status" }, /* @__PURE__ */ __dil.jsx("text", {
						size: "sm",
						color: "secondary"
					}, data.audioLabels.error)) : null));
				}
			})
		}));
	}

//#endregion
//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) {
			if (result.status === "pending") return {
				status: "pending",
				resolutionComplete: false,
				state: void 0
			};
			return result.status === "resolved" ? {
				status: "resolved",
				resolutionComplete: true,
				state: result.state
			} : {
				status: "failed",
				resolutionComplete: true,
				state: void 0
			};
		}
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "pending") return true;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region LearningVocabCard.dil.tsx
	function LearningVocabCard(props) {
		const { state } = useResolvedComponentResult("LearningVocabCard", props);
		if (state) return /* @__PURE__ */ __dil.jsx(LearningVocabCard$1, state);
		if ("progressLabels" in props) return /* @__PURE__ */ __dil.jsx(LearningVocabCard$1, props);
		return props.fallback ?? null;
	}

//#endregion
return LearningVocabCard;
})();
return __dilDefaultExport;
})()))\`,Link:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentLink(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "Link",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "Link", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentLink, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region Link.dil.tsx
	function Link(props) {
		const needsResolution = !props.url || !props.title;
		const { resolutionComplete, state } = useResolvedComponentResult("Link", props);
		if (!state && needsResolution) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("loading", { label: "Loading link" });
		const title = state?.title ?? props.title ?? props.url ?? "Link";
		const url = state?.url ?? props.url ?? "";
		const finalWordStart = /\\\\S+\\\\s*$/u.exec(title)?.index ?? 0;
		const leadingTitle = title.slice(0, finalWordStart);
		const trailingTitle = title.slice(finalWordStart);
		return /* @__PURE__ */ __dil.jsx("pressable", {
			ariaLabel: \\\`Open \\\${title}\\\`,
			inline: true,
			onClick: () => GenUI.openUrl(url)
		}, /* @__PURE__ */ __dil.jsx("text", { inline: true }, leadingTitle ? /* @__PURE__ */ __dil.jsx("text", {
			inline: true,
			underline: "dotted"
		}, leadingTitle) : null, /* @__PURE__ */ __dil.jsx("text", {
			inline: true,
			width: "max-content"
		}, /* @__PURE__ */ __dil.jsx("text", {
			inline: true,
			underline: "dotted"
		}, trailingTitle), "\\\\xA0", /* @__PURE__ */ __dil.jsx("icon", {
			inline: true,
			name: "arrow-up-right"
		}))));
	}

//#endregion
return Link;
})();
return __dilDefaultExport;
})()))\`,LinkCard:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentLinkCard(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "LinkCard",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "LinkCard", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentLinkCard, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region LinkCard.dil.tsx
	function LinkCard(props) {
		const needsResolution = !props.url || !props.title;
		const { resolutionComplete, state } = useResolvedComponentResult("LinkCard", props);
		if (!state && needsResolution) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("loading", { label: "Loading link" });
		const title = state?.title ?? props.title ?? props.url ?? "Link";
		const url = state?.url ?? props.url ?? "";
		const source = state?.source_label ?? props.subtitle;
		const snippet = state?.snippet ?? props.snippet;
		return /* @__PURE__ */ __dil.jsx("pressable", {
			ariaLabel: \\\`Open \\\${title}\\\`,
			onClick: () => GenUI.openUrl(url)
		}, /* @__PURE__ */ __dil.jsx("card", {
			gap: 2,
			size: "sm"
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 2,
			minWidth: 0,
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("favicon", {
			frame: true,
			size: 20,
			url
		}), source ? /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			maxLines: 1,
			size: "xs",
			truncate: true
		}, source) : null), /* @__PURE__ */ __dil.jsx("text", {
			color: "prose",
			maxLines: 2,
			size: "sm",
			truncate: true,
			weight: "medium"
		}, title), snippet ? /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			maxLines: 2,
			size: "sm",
			truncate: true
		}, snippet) : null));
	}

//#endregion
return LinkCard;
})();
return __dilDefaultExport;
})()))\`,MediaFallback:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentMediaFallback(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "MediaFallback",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "MediaFallback", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentMediaFallback, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region MediaFallback.dil.tsx
	function MediaFallback({ children, height = "100%", width = "100%" }) {
		return /* @__PURE__ */ __dil.jsx("box", {
			align: "center",
			background: "surface-tertiary",
			height,
			justify: "center",
			width
		}, children);
	}

//#endregion
return MediaFallback;
})();
return __dilDefaultExport;
})()))\`,MemoryCite:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentMemoryCite(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "MemoryCite",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "MemoryCite", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentMemoryCite, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region MemoryCite.dil.tsx
	function MemoryCite() {
		return null;
	}

//#endregion
return MemoryCite;
})();
return __dilDefaultExport;
})()))\`,MessageReaction:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentMessageReaction(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "MessageReaction",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "MessageReaction", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentMessageReaction, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region MessageReaction.dil.tsx
	function MessageReaction() {
		return null;
	}

//#endregion
return MessageReaction;
})();
return __dilDefaultExport;
})()))\`,NewsCarousel:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentNewsCarousel(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "NewsCarousel",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "NewsCarousel", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentNewsCarousel, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region NewsCarousel.dil.tsx
	function NewsCarousel(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("NewsCarousel", props);
		const isSm = DIL.useBreakpoint("sm");
		if (!state) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("loading", { label: "Loading news" });
		const items = state.items ?? [];
		return items.length ? /* @__PURE__ */ __dil.jsx("col", {
			gap: 3,
			width: "100%"
		}, state.title ? /* @__PURE__ */ __dil.jsx("text", { strong: true }, state.title) : null, /* @__PURE__ */ __dil.jsx("carousel", {
			ariaLabel: "News",
			gap: 4
		}, items.map((item) => /* @__PURE__ */ __dil.jsx("carousel-item", {
			background: "surface-elevated",
			key: item.ref,
			padding: 0,
			variant: "outline",
			width: isSm ? "31.5%" : "47.5%"
		}, /* @__PURE__ */ __dil.jsx("pressable", {
			onClick: () => GenUI.openUrl(item.url),
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("col", {
			gap: 4,
			height: "100%",
			padding: { bottom: 6 },
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("box", {
			align: "center",
			background: "surface-secondary",
			clip: true,
			height: "144px",
			justify: "center",
			width: "100%"
		}, item.thumbnail_url ? /* @__PURE__ */ __dil.jsx("image", {
			alt: item.title,
			fit: "cover",
			height: "100%",
			radius: "none",
			src: item.thumbnail_url,
			width: "100%"
		}) : /* @__PURE__ */ __dil.jsx("favicon", {
			frame: true,
			size: 40,
			url: item.url
		})), /* @__PURE__ */ __dil.jsx("col", {
			gap: 2,
			padding: { x: 4 },
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 1
		}, /* @__PURE__ */ __dil.jsx("favicon", {
			frame: true,
			size: 16,
			url: item.url
		}), /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			maxLines: 1,
			size: "xs",
			truncate: true
		}, item.source_label)), /* @__PURE__ */ __dil.jsx("text", {
			maxLines: 5,
			size: "sm"
		}, item.title), item.date_label ? /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			size: "xs"
		}, item.date_label) : null))))))) : null;
	}

//#endregion
return NewsCarousel;
})();
return __dilDefaultExport;
})()))\`,ProductCard:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentProductCard(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "ProductCard",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "ProductCard", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentProductCard, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region shared.dil.ts
	function useResolvedComponentResult(componentName, props) {
		const opGenui = DIL.useAppData((appData) => appData.opGenui);
		if (props.__state !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: props.__state
		};
		const key = componentDataKey(componentName, props);
		const result = (typeof props.__resolutionId === "string" ? opGenui?.componentResults?.[props.__resolutionId] : void 0) ?? opGenui?.componentResults?.[key];
		if (isComponentResultEnvelope(result)) return result.status === "resolved" ? {
			status: "resolved",
			resolutionComplete: true,
			state: result.state
		} : {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		const legacyState = opGenui?.componentData?.[key];
		if (legacyState !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: legacyState
		};
		if (opGenui?.componentResults !== void 0) return {
			status: "failed",
			resolutionComplete: true,
			state: void 0
		};
		if (opGenui?.componentData !== void 0) return {
			status: "resolved",
			resolutionComplete: true,
			state: void 0
		};
		return {
			status: "pending",
			resolutionComplete: false,
			state: void 0
		};
	}
	function isComponentResultEnvelope(value) {
		if (typeof value !== "object" || value == null || Array.isArray(value)) return false;
		const result = value;
		if (result.status === "resolved") return typeof result.state === "object" && result.state != null && !Array.isArray(result.state);
		return result.status === "failed" && (result.code === "component_unavailable" || result.code === "tool_failed" || result.code === "invalid_tool_result") && typeof result.retryable === "boolean";
	}
	function componentDataKey(componentName, props) {
		return JSON.stringify([componentName, normalizeComponentProps(props)]);
	}
	function normalizeComponentProps(props) {
		const normalized = {};
		for (const key of Object.keys(props).sort()) {
			if (key === "__resolutionId" || key === "__state" || key === "children" || key === "fallback") continue;
			const value = normalizeComponentValue(props[key]);
			if (value !== void 0) normalized[key] = value;
		}
		return normalized;
	}
	function normalizeComponentValue(value) {
		if (value === null || typeof value === "boolean" || typeof value === "number" || typeof value === "string") return value;
		if (Array.isArray(value)) return value.map(normalizeComponentValue);
		if (typeof value === "object") {
			const normalized = {};
			for (const key of Object.keys(value).sort()) {
				const nestedValue = normalizeComponentValue(value[key]);
				if (nestedValue !== void 0) normalized[key] = nestedValue;
			}
			return normalized;
		}
	}

//#endregion
//#region ProductCard.dil.tsx
	function ProductCard(props) {
		const { resolutionComplete, state } = useResolvedComponentResult("ProductCard", props);
		if (!state) return resolutionComplete ? null : /* @__PURE__ */ __dil.jsx("loading", { label: "Loading product" });
		if (!state.image_url) return null;
		const onClick = () => GenUI.openEntityDetail("product", state.title, { product: state.product });
		if (state.render_as === "item") return /* @__PURE__ */ __dil.jsx("pressable", {
			ariaLabel: \\\`View details for \\\${state.title}\\\`,
			height: "100%",
			onClick,
			padding: { x: 2 },
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("col", {
			gap: 2,
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("box", {
			aspectRatio: "1 / 1",
			radius: "lg",
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("image", {
			alt: state.title,
			fit: "contain",
			height: "100%",
			radius: "lg",
			src: state.image_url,
			width: "100%"
		})), /* @__PURE__ */ __dil.jsx("col", {
			gap: 1,
			minWidth: 0,
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("title", {
			size: "sm",
			weight: "semibold"
		}, state.title), state.price || state.description ? /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			maxLines: 3,
			size: "sm"
		}, state.price ? /* @__PURE__ */ __dil.jsx("text", {
			inline: true,
			strong: true
		}, state.price) : null, state.description ? /* @__PURE__ */ __dil.jsx("text", { inline: true }, state.price ? " • " : "", state.description) : null) : null)));
		if (state.render_as === "hero") return /* @__PURE__ */ __dil.jsx("card", { width: "100%" }, /* @__PURE__ */ __dil.jsx("pressable", {
			ariaLabel: \\\`View details for \\\${state.title}\\\`,
			onClick
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 4,
			wrap: "nowrap",
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("box", {
			aspectRatio: "4 / 3",
			maxWidth: "38%",
			minWidth: 144,
			width: 260
		}, /* @__PURE__ */ __dil.jsx("image", {
			alt: state.title,
			fit: "contain",
			height: "100%",
			radius: "lg",
			src: state.image_url,
			width: "100%"
		})), /* @__PURE__ */ __dil.jsx("col", {
			flex: "auto",
			gap: 2,
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("title", {
			size: "lg",
			weight: "semibold"
		}, state.title), /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 2,
			wrap: "wrap"
		}, state.price ? /* @__PURE__ */ __dil.jsx("text", { weight: "semibold" }, state.price) : null, state.merchant ? /* @__PURE__ */ __dil.jsx("text", { color: "secondary" }, state.merchant) : null), state.description ? /* @__PURE__ */ __dil.jsx("text", null, state.description) : null))));
		return /* @__PURE__ */ __dil.jsx("card", { width: "100%" }, /* @__PURE__ */ __dil.jsx("pressable", {
			ariaLabel: \\\`View details for \\\${state.title}\\\`,
			onClick
		}, /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 3,
			wrap: "nowrap",
			width: "100%"
		}, /* @__PURE__ */ __dil.jsx("box", {
			aspectRatio: "4 / 3",
			maxWidth: "32%",
			minWidth: 96,
			width: 132
		}, /* @__PURE__ */ __dil.jsx("image", {
			alt: state.title,
			fit: "contain",
			height: "100%",
			radius: "lg",
			src: state.image_url,
			width: "100%"
		})), /* @__PURE__ */ __dil.jsx("col", {
			flex: "auto",
			gap: 1,
			minWidth: 0
		}, /* @__PURE__ */ __dil.jsx("title", {
			size: "md",
			weight: "semibold"
		}, state.title), /* @__PURE__ */ __dil.jsx("row", {
			align: "center",
			gap: 2,
			wrap: "wrap"
		}, state.price ? /* @__PURE__ */ __dil.jsx("text", {
			size: "sm",
			weight: "semibold"
		}, state.price) : null, state.merchant ? /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			size: "sm"
		}, state.merchant) : null), state.description ? /* @__PURE__ */ __dil.jsx("text", { size: "sm" }, state.description) : null))));
	}

//#endregion
return ProductCard;
})();
return __dilDefaultExport;
})()))\`,SpeechSynthesizer:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentSpeechSynthesizer(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "SpeechSynthesizer",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "SpeechSynthesizer", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentSpeechSynthesizer, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region SpeechSynthesizer.dil.tsx
	const unavailable = {
		available: false,
		status: "idle",
		error: null,
		play: () => {},
		stop: () => {}
	};
	const { useSpeechSynthesizer = () => unavailable } = DIL;
	function SpeechSynthesizer({ controls, disabled = false, logging, ...source }) {
		const speech = useSpeechSynthesizer(source, logging);
		DIL.useEffect(() => {
			if (disabled) speech.stop();
		}, [disabled, speech.stop]);
		const play = () => {
			if (!disabled) speech.play();
		};
		const stop = () => {
			if (!disabled) speech.stop();
		};
		if (controls) return controls({
			...speech,
			play,
			stop
		});
		if (!speech.available) return /* @__PURE__ */ __dil.jsx("text", null, "Audio unavailable");
		return speech.status === "idle" ? /* @__PURE__ */ __dil.jsx("button", {
			disabled,
			onClick: play
		}, "Play") : /* @__PURE__ */ __dil.jsx("button", { onClick: stop }, "Stop");
	}

//#endregion
return SpeechSynthesizer;
})();
return __dilDefaultExport;
})()))\`,WritingBlock:\`((createComponent) => {
    const hostGenUI = GenUI;
    const unscopedComponent = createComponent(DIL, hostGenUI);
    function ResolvedComponentWritingBlock(props) {
      const resolutionId = props.__resolutionId;
      const Component = DIL.useMemo(() => {
        if (typeof resolutionId !== "string") return unscopedComponent;
        const widgetGenUI = Object.freeze({
          ...hostGenUI,
          dispatchAction(action) {
            if (action.handler !== "server") return hostGenUI.dispatchAction(action);
            return hostGenUI.dispatchWidgetAction({
              type: action.type,
              payload: action.payload,
              nativeWidgetResolutionId: resolutionId,
              componentName: "WritingBlock",
            });
          },
        });
        const widgetDIL = Object.freeze({
          ...DIL,
          useState(initialState, options) {
            return DIL.useState(initialState, { ...options, widgetId: resolutionId });
          },
        });
        return createComponent(widgetDIL, widgetGenUI);
      }, [resolutionId]);
      return __dil.jsx(OpGenuiResolvedComponentBoundary, { componentName: "WritingBlock", componentProps: props, Component });
    }
    return Object.assign(ResolvedComponentWritingBlock, unscopedComponent);
  })((DIL, GenUI) => ((() => {
var __dilDefaultExport = (function() {


//#region WritingBlock.dil.tsx
	function WritingBlock({ children, content, subject, variant }) {
		const body = content ?? children;
		return /* @__PURE__ */ __dil.jsx("card", { size: "full" }, /* @__PURE__ */ __dil.jsx("col", { gap: 2 }, variant === "email" && subject ? /* @__PURE__ */ __dil.jsx(__dil.Fragment, null, /* @__PURE__ */ __dil.jsx("col", { gap: 1 }, /* @__PURE__ */ __dil.jsx("text", {
			color: "secondary",
			size: "sm",
			weight: "medium"
		}, "Subject"), /* @__PURE__ */ __dil.jsx("text", { weight: "semibold" }, subject)), /* @__PURE__ */ __dil.jsx("divider", null)) : /* @__PURE__ */ __dil.jsx("title", null, "Writing Block"), /* @__PURE__ */ __dil.jsx("text", { preserveWhitespace: true }, body)));
	}

//#endregion
return WritingBlock;
})();
return __dilDefaultExport;
})()))\`})})(Mt,Mt.exports)),Mt.exports}var Oe;function Rn(){if(Oe)return Jt;Oe=1;const{DIL_COMPONENT_SOURCES:a}=Ae();return Jt.DIL_COMPONENT_SOURCES=a,Jt}var Wt={},ze;function Cn(){if(ze)return Wt;ze=1;const a=Object.freeze(typeof __RESTRICTED__<"u"&&__RESTRICTED__?["ArtistUpcomingEventsWidget","AskUserFiles","AskUserLocation","AskUserLocationV2","AskUserLocationV2Bidi","AsyncVideoCarousel","Bento","BusinessGalleryRefWidget","BusinessGalleryWidget","CalculatorAbcd2ScoreForTransientIschemicAttackWidget","CalculatorApacheIiScoreWidget","CalculatorApgarScoreWidget","CalculatorAsaPhysicalStatusClassificationWidget","CalculatorBilitoolWidget","CalculatorBisapScoreForPancreatitisWidget","CalculatorBishopScoreForCervicalRipeningWidget","CalculatorCanadianCtHeadRuleWidget","CalculatorCha2ds2VascScoreForAtrialFibrillationStrokeRiskWidget","CalculatorChildPughScoreForCirrhosisWidget","CalculatorCorrectedAdjustedAgeOfPretermInfantsWidget","CalculatorCurb65ScoreForPneumoniaWidget","CalculatorDukeTreadmillScoreWidget","CalculatorFourTScoreForHeparinInducedThrombocytopeniaWidget","CalculatorGlasgowBlatchfordScoreForUpperGiBleedingWidget","CalculatorGlasgowComaScaleGcsWidget","CalculatorGraceScoreForAcuteCoronarySyndromeWidget","CalculatorHasBledScoreForMajorBleedingRiskWidget","CalculatorHeartScoreForMajorCardiacEventsWidget","CalculatorHuntAndHessScoreForSubarachnoidHemorrhageWidget","CalculatorIchScoreWidget","CalculatorIsthDicScoreWidget","CalculatorModifiedRankinScaleWidget","CalculatorNexusCervicalSpineRuleWidget","CalculatorNihStrokeScaleNihssWidget","CalculatorOttawaAnkleRuleWidget","CalculatorOttawaKneeRuleWidget","CalculatorPaduaPredictionScoreForVenousThromboembolismRiskWidget","CalculatorPecarnPediatricHeadInjuryRuleWidget","CalculatorPediatricEarlyWarningScorePewsWidget","CalculatorPercRuleForPulmonaryEmbolismWidget","CalculatorPneumoniaSeverityIndexPsiPortWidget","CalculatorQsofaScoreWidget","CalculatorRansonCriteriaForPancreatitisWidget","CalculatorRevisedCardiacRiskIndexRcriWidget","CalculatorRevisedGenevaScoreForPulmonaryEmbolismWidget","CalculatorRochesterCriteriaForFebrileInfantsWidget","CalculatorRockallScoreForUpperGiBleedingWidget","CalculatorRoxIndexForHighFlowNasalCannulaWidget","CalculatorSirsCriteriaWidget","CalculatorSofaScoreWidget","CalculatorStopBangScoreForObstructiveSleepApneaWidget","CalculatorSurgicalApgarScoreWidget","CalculatorTimiRiskWidget","CalculatorWellsScoreForDeepVeinThrombosisWidget","CalculatorWellsScoreForPulmonaryEmbolismWidget","CalculatorWestleyCroupSeverityScoreWidget","CalculatorWidget","CalculatorYearsAlgorithmForPulmonaryEmbolismWidget","CalendarEventsWidget","CalendarList","CareProviderResultsWidget","CareProviderSidebarWidget","CfbGames","ChecklistWidget","ClockWidget","ConversationFollowUpActionList","ConversationalOnboardingAdvice","ConversationalOnboardingFollowUpPills","ConversationalOnboardingSearch","ConversationalOnboardingStudy","ConversationalOnboardingWriting","CopyWordsWidget","CricketSingleMatch","CurrencyConverterV2WidgetWithSource","CurrencyConverterWidgetWithSource","DigitalStopwatchWidget","DigitalTimerWidget","DraftEmail","EmailPreview","EntityCard","EntityExplorer","EntityOverviewCarouselWidget","EntityOverviewWidget","EntityReviewsRefWidget","EntityReviewsWidget","EntityThumbnailList","EplScheduleWidgetWithSource","EplStandingsWidgetWithSource","EventSidebarWidget","ExampleServerActionButton","F1RacesWidgetWithSource","F1StandingsWidgetWithSource","FollowUpChoiceCard","FollowUpLinkList","FollowUpPillGroup","FollowUpQuiz","Gpt4oSystemCard","Gpt5SystemCard","HolidayWidget","HomeworkHelperV2","ImageGenWidget","ImageGrid","InstantSuggestionsWidget","InternalPartnerAppWidget","IplScheduleWidgetWithSource","IplStandingsWidgetWithSource","JobsWidget","LearningAudioLabelCard","LearningFillBlankCard","LearningFlashcards","LearningImageChoiceCard","LearningImageLabelCard","LearningQuiz","LearningSentenceBuilderCard","LedgerAccountBreakdownWidget","LedgerAccountsWidget","LedgerCreditScoreDetail","LedgerCreditScoreWidget","LedgerEquityUpdatesWidget","LedgerFeeInterestPaidWidget","LedgerFinanceOnboardingWidget","LedgerFinanceStatusBannerWidget","LedgerIncomeTrackerWidget","LedgerLowConfidenceCategoryCorrection","LedgerLowConfidenceCategoryCorrectionLegacyWidget","LedgerNetWorthWidget","LedgerPortfolioDistributionWidget","LedgerRecentTransactionsWidget","LedgerRecurringTransactionsWidget","LedgerRepairBankConnectionWidget","LedgerSpendByCategoryDetail","LedgerSpendByCategoryRecentTransactions","LedgerSpendByCategoryWidget","LedgerSpendSoFarThisMonthWidget","LedgerTransactionDetail","LedgerUpcomingActivityRecentTransactions","LedgerUpcomingActivityWidget","LedgerWatchlistWidget","LocalBusinessWidget","LyricsWidgetWithSource","MarchMadness","MlbScheduleWidgetWithSource","MlbStandingsWidgetWithSource","MultipleChoiceBlockV2","NbaGameBoxscoreWidgetWithNbaGameBoxScoreSource","NbaPlayerSummaryWidgetWithSource","NbaScheduleWidgetWithSource","NbaScores","NbaStandingsWidgetWithSource","NcaambScheduleWidgetWithSource","NcaambStandingsWidgetWithSource","NcaawbScheduleWidgetWithSource","NcaawbStandingsWidgetWithSource","NewsArticleWidget","NewsArticleWidgetCarousel","NflGames","NflScheduleWidgetWithSource","NflStandingsWidgetWithSource","NhlScheduleWidgetWithSource","NhlStandingsWidgetWithSource","OfferScheduledPrompt","OfferVoiceConversation","OnboardingFeatureCard","OnboardingSelectionCard","OpenDetailWidget","OpenLearningFlashcards","OpenaiWidget","PackageTracker","PersistedWidgetDogfoodChecklistWidgetWithSource","PersistedWidgetDogfoodDailyNutritionWidgetWithSource","PersistedWidgetDogfoodExerciseLogWidgetWithSource","PersistedWidgetDogfoodGoalWidgetWithSource","PersonWidget","PersonWidgetAlwaysTrigger","PlacesMetadataBar","ProductCarousel","PromptChecklist","PulseDraftEmail","PulseExpansionCard","Rating","Reddit","RedditFromProductSearch","RedditFromSearch","ReservationTimePillsRefCarouselWidget","ReservationTimePillsRefWidget","RestaurantMenuRefWidget","RestaurantMenuWidget","SidebarFactTable","SidebarPeopleAlsoAsk","SoccerGames","StockChart","StockComparisonChart","StockHeatmap","SuggestAutomation","SuperbowlRiddleWidget","TabGroup","TabbedSectionWidget","TaskAutopauseCard","TaskExpansionCard","TennisPlayerSummaryWidgetWithSource","UnitConverterV2Widget","UnitConverterWidget","UpdateLearningFlashcards","VisualCardCarouselWidget","WeatherCurrentWithSource","WeatherSidebarTitleWidgetWithSource","WeatherWidgetV1WithSource","WeatherWidgetV3WithSource","WeatherWidgetWithSource","WebLinksCarousel","WnbaScheduleWidgetWithSource","WnbaStandingsWidgetWithSource","WordCardWidget"]:[]);return Wt.DIL_LAZY_COMPONENT_NAMES=a,Wt.DIL_COMPONENT_NAMES=Object.freeze(["OpGenuiResolvedComponentBoundary","AsyncImage","AsyncImageGroup","AsyncVideo","AutomationPlanSummary","Citation","Cite","CoTToolGroup","CodeBlock","CodeCite","Entity","FileCite","FileNavList","FlightCard","FlightCarousel","FlightTracker","FollowUp","FollowUpActionBar","GenImage","LearningSpeakCard","LearningVizDil","LearningVocabCard","Link","LinkCard","MediaFallback","MemoryCite","MessageReaction","NewsCarousel","ProductCard","SpeechSynthesizer","WritingBlock",...a]),Wt}var Ge;function En(){return Ge||(Ge=1,(function(a,t){a.path="dil_sandbox/src/DILComponentSources",Object.defineProperty(t,"__esModule",{value:!0}),t.getDILComponentSource=t.isDILRestrictedComponent=t.getDILComponentNames=t.DIL_COMPONENT_SOURCES=void 0;const n=Rn(),r=Ae(),l=Cn();t.DIL_COMPONENT_SOURCES=n.DIL_COMPONENT_SOURCES;function o(){return l.DIL_COMPONENT_NAMES}t.getDILComponentNames=o;function u(s){return l.DIL_COMPONENT_NAMES.includes(s)&&!Object.prototype.hasOwnProperty.call(r.DIL_COMPONENT_SOURCES,s)}t.isDILRestrictedComponent=u;function d(s,m){var h;const x=(h=n.DIL_COMPONENT_SOURCES[s])!==null&&h!==void 0?h:m.get(s);if(x===void 0)throw new Error(\`DIL sandbox component '\${s}' is not loaded.\`);return x}t.getDILComponentSource=d})(Nt,Nt.exports)),Nt.exports}var Ne;function wn(){return Ne||(Ne=1,(function(a,t){a.path="dil_sandbox/src/DILSandboxGlobals",Object.defineProperty(t,"__esModule",{value:!0}),t.hardenSandboxGlobal=t.lockDownSandboxGlobal=t.getDILSandboxAllowedGlobalNames=void 0;const n=ot(),r=En(),l=new Set(["AggregateError","Array","ArrayBuffer","Atomics","BigInt","BigInt64Array","BigUint64Array","Boolean","DataView","Date","DebuggerInternal","Error","EvalError","FinalizationRegistry","Float16Array","Float32Array","Float64Array","Function","HermesES6Internal","HermesInternal","Infinity","Intl","Int8Array","Int16Array","Int32Array","Iterator","JSON","Map","Math","NaN","Number","Object","Promise","Proxy","RangeError","ReferenceError","Reflect","RegExp","Set","SharedArrayBuffer","String","Symbol","SyntaxError","TextEncoder","TEMPORARY","PERSISTENT","TypeError","URIError","Uint8Array","Uint8ClampedArray","Uint16Array","Uint32Array","WeakMap","WeakRef","WeakSet","decodeURI","decodeURIComponent","encodeURI","encodeURIComponent","escape","eval","globalThis","isFinite","isNaN","onmessage","parseFloat","parseInt","undefined","unescape",...(0,r.getDILComponentNames)()]);function o(){return Array.from(l)}t.getDILSandboxAllowedGlobalNames=o;function u(h){s(h,!0),Object.freeze(h)}t.lockDownSandboxGlobal=u;function d(h,x){s(h,!1),x?(0,n.freezeObjectGraph)(h,!0):Object.freeze(h)}t.hardenSandboxGlobal=d;function s(h,x){for(let _=h;_!==null&&_!==Object.prototype;_=Object.getPrototypeOf(_))for(const e of Reflect.ownKeys(_))typeof e=="string"&&(x||e!=="onmessage")&&l.has(e)||m(_,e)}function m(h,x){try{Reflect.set(h,x,void 0)}catch{}try{Reflect.deleteProperty(h,x)}catch{}const _=Object.getOwnPropertyDescriptor(h,x);if(!(_===void 0||"value"in _&&_.value===void 0))throw new Error(\`Failed to remove sandbox global '\${String(x)}'.\`)}})(Gt,Gt.exports)),Gt.exports}var Dn=wn(),Ft={exports:{}},Me;function Sn(){return Me||(Me=1,(function(a,t){a.path="dil_sandbox/src/DILSandboxRegExp",Object.defineProperty(t,"__esModule",{value:!0}),t.createSandboxRegExp=void 0;function n(r){var l;const o=Reflect.construct,u=Reflect.apply,d=r.prototype,s=(l=Object.getOwnPropertyDescriptor(d,"source"))===null||l===void 0?void 0:l.get;if(s===void 0)throw new Error("Sandbox RegExp requires the native source getter.");const m=s,h=function(e,c){const f=new.target;return f===void 0&&c===void 0&&_(e)&&e.constructor===x?e:o(r,[e,c],f===void 0||f===h?x:f)};Object.defineProperty(h,"prototype",{value:d});const x=h.bind(void 0);function _(e){if(e===null||typeof e!="object"&&typeof e!="function")return!1;const c=e[Symbol.match];if(c!==void 0)return!!c;try{return u(m,e,[]),e!==d}catch{return!1}}Object.defineProperties(x,{name:{value:"RegExp",configurable:!0},length:{value:2,configurable:!0},prototype:{value:d}});for(const e of["escape",Symbol.species]){if(e===void 0)continue;const c=Object.getOwnPropertyDescriptor(r,e);c!==void 0&&Object.defineProperty(x,e,c)}return Object.defineProperty(d,"constructor",{value:x}),x}t.createSandboxRegExp=n})(Ft,Ft.exports)),Ft.exports}var kn=Sn();const Un="__oaiDilWebElementProps";class jn extends fn.DILSandboxRuntime{pendingRenderFlushes;render(t){const n=[];this.pendingRenderFlushes=n;try{const r=super.render(t);if(r.encoded!==void 0&&!r.errors?.length)for(const l of n)super.scheduleFlush(l);return r}finally{this.pendingRenderFlushes=void 0}}scheduleFlush(t){this.pendingRenderFlushes?this.pendingRenderFlushes.push(t):super.scheduleFlush(t)}}class Ln{appDataProxies=new WeakMap;data;disposed=!1;dirty=!1;flushScheduled=!1;globals;dilComponents;lastSource;options;wrappedCallbacks=new WeakMap;renderedPropNames=[];rendering=!1;dispatchingCallback=!1;runtime;timeStore=new gn;types=[];wrappedDil=new WeakMap;wrappedJsx=new WeakMap;constructor(t,n){this.options=n;const r=()=>!this.disposed&&!this.rendering&&this.dispatchingCallback,l=xn(n,r),o=In(n,r);this.globals=n.globals??{},this.dilComponents=n.dilComponents??{},this.data=Qt(n.data);for(const s of Object.keys(this.globals))if(We(s),s==="DIL"||s==="__dil")throw new Error(\`DIL global "\${s}" is reserved\`);const u=new Map,d=new Proxy(Object.create(null),{get:(s,m)=>{if(typeof m!="string")return;let h=u.get(m);return h||(h={childrenMode:Ee.DILElementChildrenMode.RenderedNodes,primitiveTextProp:void 0,renderedPropNames:this.renderedPropNames,typeId:this.types.push(m)},u.set(m,h)),h}});this.runtime=this.trace("runtime_construct",()=>new jn({elementsMetadata:d,requestRender:this.scheduleRender,trace:n.trace??((s,m)=>m()),errorMode:Ee.DILRenderErrorMode.ReportAndAbortRender,stateChangeHandler:(s,m,h)=>{try{this.options.onStateChange?.(JSON.parse(s),h)}catch{}},speechSynthesis:l,dictation:o,asyncErrorHandler:n.onError,sourceEvaluatorFactory:s=>this.createSourceEvaluator(s)})),this.trace("seed_runtime",()=>this.runtime.render(this.createRenderInput())),n.stateSnapshot&&this.trace("hydrate_state",()=>this.runtime.setStateSnapshot(JSON.stringify(n.stateSnapshot)));for(const[s,m]of Object.entries(n.widgetStateSnapshots??{}))this.runtime.setStateSnapshot(JSON.stringify(m),s);this.updateSource(t)}updateSource(t,n,r){if(this.disposed)return;r&&(this.dilComponents=r);try{const o=this.trace("update_source",()=>this.runtime.updateSource(t));if(o?.length){this.restoreSource(),this.options.onError(o[0]);return}}catch(o){this.restoreSource(),this.options.onError(o);return}const l=this.data;n&&(this.data=Qt(n)),this.render()?this.lastSource=t:(this.data=l,this.restoreSource())}setData(t){this.disposed||(this.data=Qt(t),this.render())}setStateSnapshot(t,n){this.disposed||(this.trace("hydrate_state",()=>this.runtime.setStateSnapshot(t===void 0?void 0:JSON.stringify(t),n)),this.flushRender())}invokeFunction(t,n,r){try{let l;this.dispatchingCallback=!0;try{l=this.runtime.invokeFunction(t,n)}finally{this.dispatchingCallback=!1}if(this.flushRender(),!l||typeof l.then!="function")return l;const o=Promise.resolve(l).then(u=>(this.flushRender(),u),u=>{if(this.options.onError(u),r)throw u});return r?o:void 0}catch(l){if(this.options.onError(l),r)throw l;return}}registerDilMessengerTransport(t,n){return this.runtime.registerHtmlViewMessengerTransport(t,n)}dispose(){if(!this.disposed){this.disposed=!0,this.timeStore.dispose();for(const t of this.runtime.dispose()??[])this.options.onError(t)}}scheduleRender=()=>{this.dirty=!0,this.scheduleFlush()};scheduleFlush(){this.flushScheduled||this.disposed||(this.flushScheduled=!0,Promise.resolve().then(()=>{if(this.flushScheduled=!1,this.disposed)return;const t=()=>this.trace("flush",()=>{this.flushRender(),this.flushEffects(),this.flushRender()});this.options.runWithExecutionAttribution?this.options.runWithExecutionAttribution(t):t()}))}flushRender(){!this.dirty||this.disposed||this.rendering||this.trace("flush_render",()=>{for(;this.dirty&&!this.disposed&&!this.rendering;)this.dirty=!1,this.render()})}flushEffects(){try{for(const t of this.trace("flush_effects",()=>this.runtime.flush())??[])this.options.onError(t)}catch(t){this.options.onError(t)}}render(){this.rendering=!0;try{const{encoded:t,errors:n}=this.trace("render_operations",()=>this.runtime.render(this.createRenderInput()));return n?.length||!t?(this.options.onError(n?.[0]??new Error("DIL render failed")),!1):(this.options.onOperations({...t,types:[...this.types]}),this.trace("commit_state",()=>this.runtime.commitStateSnapshot()),this.scheduleFlush(),!0)}catch(t){return this.options.onError(t),!1}finally{this.rendering=!1}}createRenderInput(){let t=this.appDataProxies.get(this.data.appData);return t||(Object.hasOwn(this.globals,"__modelGenUI")&&mt.freezeObjectGraph(this.data.appData,!1),t=new Proxy(this.data.appData,{get:(n,r,l)=>r==="__dilHost"?{isMobile:this.data.isMobile===!0}:Reflect.get(n,r,l)}),this.appDataProxies.set(this.data.appData,t)),{activeBreakpoints:this.data.activeBreakpoints,animated:!1,appData:t,constants:this.data.constants,contextId:0,kind:3,stateSnapshot:void 0,theme:this.data.theme}}wrapDilRuntime=t=>{const n=this.wrappedDil.get(t);if(n)return n;const r=mt.hardenApiFacade({...t,useNow:(l=!0,o=1e3)=>{if(typeof l!="boolean")throw new TypeError("DIL.useNow expects a boolean");if(o!==50&&o!==1e3)throw new TypeError("DIL.useNow expects intervalMs to be 50 or 1000");return t.useEffect(()=>l?this.timeStore.subscribe(this.scheduleRender,o):void 0,[l,o]),Date.now()},render:l=>{if(!ht.isDILRuntimeElement(l)||typeof l.type!="function"||typeof l.props?.key!="string"||!/^body:\\d+$/.test(l.props.key))return t.render(l);const o={...l.props};return delete o.key,t.render(ht.createDilElement(l.type,o,l.children))}});return this.wrappedDil.set(t,r),r};wrapJsxRuntime=t=>{const n=this.wrappedJsx.get(t);if(n)return n;const r=mt.hardenApiFacade({Fragment:t.Fragment,jsx:(l,o,...u)=>{if(typeof l!="string")return t.jsx(l,o,...u);const d={...o};for(const[s,m]of Object.entries(d)){typeof m=="function"&&!Object.isExtensible(m)&&(d[s]=this.wrapCallback(m)),ht.isDILRuntimeElement(m)&&!this.renderedPropNames.includes(s)&&this.renderedPropNames.push(s);const h=Ze.getDILHtmlViewMessengerHost(m);h&&(d[s]={__dilMessenger:"html-view",channelId:h.channelId,command:h.command,commands:{},onEvent:h.event})}return t.jsx(l,{...d,[Un]:Object.keys(d).filter(s=>s!=="key")},...u)}});return this.wrappedJsx.set(t,r),r};wrapCallback(t){const n=this.wrappedCallbacks.get(t);if(n)return n;const r=(...l)=>t(...l);return this.wrappedCallbacks.set(t,r),r}createSourceEvaluator(t){const n=this.wrapJsxRuntime(t.jsxRuntimeFacade),r=this.globals.__modelGenUI,l=r!=null&&typeof r=="object",o=this.globals.GenUI,u=o?.dispatchWidgetClientAction,d=o?.issueNewTurn,s=l&&typeof u=="function",m={...this.globals,DIL:this.wrapDilRuntime(t.widgetRuntimeApiFacade),__dil:n,...s?{__bindWidgetGenUI:e=>mt.hardenApiFacade({...o,dispatchAction:c=>u(e,c),...typeof d=="function"?{issueNewTurn:(c,f)=>d(c,f,e)}:{}})}:{}},h={GenUI:t.modelGenUIFacade,Date:t.dateConstructor,...this.globals,...l?{GenUI:r}:{},DIL:this.wrapDilRuntime(t.runtimeApiFacade),__dil:n},x=Object.create(null),_=Object.create(null);return{beginRender(){},evaluate:e=>{const c=Object.fromEntries(Object.entries(this.dilComponents).filter(([w])=>!Object.hasOwn(x,w)));if(Object.keys(c).length){const P=new Function(...Object.keys(m),...Object.keys(x),this.trace("prepare_source",()=>this.prepareWidgetSource(c,s)))(...Object.values(m),...Object.values(x));for(const[O,V]of Object.entries(P))if(l){if(typeof V!="function")throw new TypeError(\`DIL component "\${O}" must be a function\`);_[O]=Le(O,V,n,()=>this.data.appData)}else _[O]=V;Object.assign(x,P)}const f={...h,..._},g=t.trace("DILRenderer.compileJS",()=>new Function(...Object.keys(f),\`"use strict";
\${e}\`));t.trace("DILRenderer.evaluateJS",()=>g(...Object.values(f)))},dispose(){}}}prepareWidgetSource(t,n){const r=Object.entries(t).map(([o,u])=>{if(We(o),!/^[A-Z]/.test(o))throw new Error(\`DIL component name "\${o}" must start with uppercase\`);if(o==="DIL"||o==="__dil")throw new Error(\`DIL component name "\${o}" is reserved\`);if(Object.prototype.hasOwnProperty.call(this.globals,o))throw new Error(\`DIL component name "\${o}" conflicts with a global\`);return n?\`const \${o}=((GenUI)=>(\${u}))(__bindWidgetGenUI(\${JSON.stringify(o)}));\`:\`const \${o}=(\${u});\`}),l=Object.keys(t);return\`"use strict";\${r.join(\`
\`)}
return {\${l.join(",")}};\`}trace(t,n){return this.options.trace?this.options.trace(t,n):n()}restoreSource(){const t=this.lastSource;t!==void 0&&this.trace("restore_source",()=>this.runtime.updateSource(t))}}function Qt(a){return{activeBreakpoints:[],appData:{},constants:{},isMobile:!1,theme:"light",...a}}function We(a){if(!/^[A-Za-z_$][0-9A-Za-z_$]*$/.test(a))throw new Error(\`Invalid DIL global name "\${a}"\`)}const Pn=EventTarget.prototype.addEventListener,An=EventTarget.prototype.removeEventListener;function dt(a,t,n){Reflect.apply(Pn,a,[t,n])}function Fe(a,t,n){Reflect.apply(An,a,[t,n])}class On{disposed=!1;commandIndex=0;eventListeners=new Map;handlers;pendingCommands=new Map;port;diagnostics;diagnosticContext;constructor({handlers:t,port:n,diagnostics:r,diagnosticContext:l}){this.handlers=t,this.port=n,this.diagnostics=r,this.diagnosticContext=l,dt(this.port,"message",this.handleMessage),this.port.start()}commands=new Proxy(Object.create(null),{get:(t,n)=>{if(!(typeof n!="string"||n==="then"))return r=>this.sendCommand(n,r)}});emit(t,n){this.send({__oaiDilMessage:!0,data:n,event:String(t),kind:"event"})}on(t,n){const r=String(t),l=this.eventListeners.get(r)??new Set;return l.add(n),this.eventListeners.set(r,l),()=>{l.delete(n),l.size===0&&this.eventListeners.delete(r)}}dispose(t="DIL frame messenger disposed"){if(this.disposed)return;this.disposed=!0,Fe(this.port,"message",this.handleMessage),this.port.close();const n=new Error(t);for(const[r,l]of this.pendingCommands)this.diagnostics?.record("command","cancelled",{...this.diagnosticContext,requestId:r,reason:"disposed"}),l.reject(n);this.pendingCommands.clear(),this.eventListeners.clear()}sendCommand(t,n){if(this.disposed)return Promise.reject(new Error("DIL frame messenger is disposed"));const r=\`command_\${this.commandIndex}\`;return this.commandIndex+=1,this.diagnostics?.record("command","begin",{...this.diagnosticContext,requestId:r,command:t,pendingRequests:this.pendingCommands.size+1}),new Promise((l,o)=>{this.pendingCommands.set(r,{reject:o,resolve:l});try{this.send({__oaiDilMessage:!0,command:t,data:n,id:r,kind:"command"})}catch(u){this.diagnostics?.fault("command",u,{...this.diagnosticContext,requestId:r,command:t}),this.pendingCommands.delete(r),o(u)}})}messageDetails(t){return{...this.diagnosticContext,command:t.kind==="event"?t.event:t.kind==="command"?t.command:void 0,requestId:"id"in t?t.id:void 0,reason:t.kind}}send(t){if(this.disposed)throw new Error("DIL frame messenger is disposed");try{this.port.postMessage(t)}catch(n){throw this.diagnostics?.fault("message",n,this.messageDetails(t)),n}this.diagnostics?.record("message","sent",this.messageDetails(t))}handleMessage=t=>{if(!Gn(t.data))return;const n=t.data;switch(this.diagnostics?.record("message","received",this.messageDetails(n)),n.kind){case"ack":this.diagnostics?.record("command","acknowledged",{...this.diagnosticContext,requestId:n.id});break;case"command":this.handleCommand(n);break;case"event":for(const r of this.eventListeners.get(n.event)??[])r(n.data);break;case"response":{const r=this.pendingCommands.get(n.id);if(!r)return;this.pendingCommands.delete(n.id),this.diagnostics?.record("command",n.error?"rejected":"resolved",{...this.diagnosticContext,requestId:n.id,pendingRequests:this.pendingCommands.size}),n.error?r.reject(zn(n.error)):r.resolve(n.result);break}}};async handleCommand(t){const n=t.command==="invokeGlobal"||t.command==="invokeMessengerCommand"?"host_call":"command",r=this.diagnostics?{...this.diagnosticContext,requestId:t.id,command:t.command}:void 0,l=Object.prototype.hasOwnProperty.call(this.handlers,t.command)?this.handlers[t.command]:void 0;if(!l){this.sendResponseError(t.id,new Error(\`Unknown DIL frame command "\${t.command}"\`));return}let o=!1;try{this.diagnostics?.record(n,"begin",r);const u=l(t.data);o=!0,this.diagnostics?.record(n,"returned",r),t.acknowledge&&!this.disposed&&this.send({__oaiDilMessage:!0,id:t.id,kind:"ack"});const d=await u;if(this.diagnostics?.record(n,"resolved",r),this.disposed)return;try{this.send({__oaiDilMessage:!0,id:t.id,kind:"response",result:d})}catch(s){this.sendResponseError(t.id,s)}}catch(u){this.diagnostics?.record(n,o?"rejected":"threw",{...r,error:u}),this.disposed||this.sendResponseError(t.id,u)}}sendResponseError(t,n){this.send({__oaiDilMessage:!0,error:Xt(n),id:t,kind:"response"})}}function Xt(a){return a instanceof Error?{message:a.message,name:a.name}:{message:String(a),name:"Error"}}function zn(a){const t=new Error(a.message);return Object.defineProperty(t,"name",{configurable:!0,enumerable:!0,value:a.name,writable:!0}),a.stack&&(t.stack=a.stack),t}function Gn(a){if(a==null||typeof a!="object"||a.__oaiDilMessage!==!0)return!1;const t=a.kind;return t==="ack"||t==="command"||t==="event"||t==="response"}function Ve(a,t){const n=r=>Nn(r)?(...l)=>t(r.__oaiDilGlobalFunction,l):Array.isArray(r)?r.map(n):r!=null&&typeof r=="object"?Object.fromEntries(Object.entries(r).map(([l,o])=>[l,n(o)])):r;return n(a)}function Nn(a){return a!=null&&typeof a=="object"&&!Array.isArray(a)&&typeof a.__oaiDilGlobalFunction=="string"}const Yt=14,Mn="__dil_confirm_startup";function Wn(a){return a!=null&&typeof a=="object"&&a.__oaiDilFrame===!0&&a.kind==="createRunner"&&a.protocolVersion===Yt&&typeof a.compiledDil=="string"&&typeof a.runnerId=="string"}function Fn(a){return a!=null&&typeof a=="object"&&a.__oaiDilFrame===!0&&a.kind==="disposeRunner"&&a.protocolVersion===Yt&&typeof a.runnerId=="string"}function Vn(a){return a!=null&&typeof a=="object"&&a.__oaiDilWorker===!0&&a.kind==="initializeControl"&&a.protocolVersion===Yt}const Tn=globalThis.crypto?.getRandomValues.bind(globalThis.crypto),Te=(a,t)=>({kind:J.DILDictationEventKind.Error,error:{code:a,message:t}}),Hn=()=>Te("host_error","Dictation could not complete. Please try again.");function qn(a){if(!a||typeof a!="object")return;const t=a;switch(t.kind){case J.DILDictationEventKind.Recording:case J.DILDictationEventKind.Stopping:case J.DILDictationEventKind.Cancelled:return{kind:t.kind};case J.DILDictationEventKind.Transcript:case J.DILDictationEventKind.Done:return typeof t.transcript=="string"?{kind:t.kind,transcript:t.transcript}:void 0;case J.DILDictationEventKind.Error:if(typeof t.error?.code=="string"&&typeof t.error.message=="string")return{kind:t.kind,error:{code:t.error.code,message:t.error.message}}}}class Bn{constructor(t){this.send=t}send;index=0;prefix=Tn?.(new Uint8Array(16)).reduce((t,n)=>t+n.toString(16).padStart(2,"0"),"");observers=new Map;createSession(t,n){if(!this.prefix)throw new Error("Dictation transport identity unavailable");const r=\`\${this.prefix}:\${this.index++}\`;this.observers.set(r,{sequence:0,receive:n});try{this.send({action:"create",id:r,options:t})}catch(o){throw this.observers.delete(r),o}let l=!1;return{start:()=>{!l&&this.observers.has(r)&&this.send({action:"start",id:r})},stop:()=>{!l&&this.observers.has(r)&&this.send({action:"stop",id:r})},cancel:()=>{this.observers.delete(r),l||this.send({action:"cancel",id:r})},dispose:()=>{l||(l=!0,this.observers.delete(r),this.send({action:"dispose",id:r}))}}}receive=({id:t,sequence:n,event:r})=>{const l=this.observers.get(t);if(!l||!Number.isSafeInteger(n)||n<=l.sequence)return;l.sequence=n;const o=qn(r)??Hn();(o.kind===J.DILDictationEventKind.Done||o.kind===J.DILDictationEventKind.Error||o.kind===J.DILDictationEventKind.Cancelled)&&this.observers.delete(t),l.receive(o)};fail(t){const n=this.observers.get(t);this.observers.delete(t),n?.receive(Te("transport_error","The dictation connection was lost."))}dispose(){const t=[...this.observers.keys()];this.observers.clear();for(const n of t)this.send({action:"dispose",id:n})}}const He=Object.freeze({status:"idle",available:!1,error:null});function $n(a){if(!a||typeof a!="object")return He;const t=a;let n=null;if(t.error&&typeof t.error=="object"){const r=t.error,l=r.failureStage;(l==="play"||l==="request"||l==="response"||l==="streaming")&&(n={failureStage:l},typeof r.httpStatus=="number"&&Number.isInteger(r.httpStatus)&&r.httpStatus>=100&&r.httpStatus<=599&&(n.httpStatus=r.httpStatus))}return{available:t.available===!0,status:n?"idle":t.status==="loading"||t.status==="playing"?t.status:"idle",error:n}}const Kn=globalThis.crypto?.getRandomValues.bind(globalThis.crypto);class Jn{constructor(t){this.send=t}send;index=0;prefix=Kn?.(new Uint8Array(16)).reduce((t,n)=>t+n.toString(16).padStart(2,"0"),"");observers=new Map;createSession(t,n){if(!this.prefix)return n(He),{play(){},stop(){},dispose(){}};const r=\`\${this.prefix}:\${this.index++}\`;return this.observers.set(r,n),this.send({action:"create",id:r,source:t}),{play:l=>this.send({action:"play",id:r,logging:l}),stop:l=>this.send({action:"stop",id:r,logging:l}),dispose:()=>{this.observers.delete(r),this.send({action:"dispose",id:r})}}}receive=({id:t,state:n})=>{this.observers.get(t)?.($n(n))}}function Qn(a,t,n){const r=a.dictation===!0?new Bn(d=>{const s=n();t.commands.dictation(d.action==="start"&&s!==void 0?{...d,interactionCapability:s}:d).catch(()=>r?.fail(d.id))}):void 0,l=r?t.on("dictationEvent",r.receive):void 0,o=a.speechSynthesis?new Jn(d=>{const s=n(),m=(d.action==="play"||d.action==="stop"&&d.logging!==void 0)&&s!=null?{...d,interactionCapability:s}:d;t.commands.speech(m).catch(()=>{o?.receive({id:d.id,state:{available:!1,status:"idle",error:null}})})}):void 0,u=o?t.on("speechState",o.receive):void 0;return{options:{speechSynthesis:o,dictation:r?{kind:"ready",service:r}:void 0},dispose:()=>{l?.(),r?.dispose(),u?.()}}}const Xn=["host_setup","encode_globals","read_persistence","acquire_frame","iframe_create","iframe_load","iframe_resource","observer_bootstrap","module_load","module_resource","module_entry","frame_ready","manager_create","worker_create","worker_control","worker_module","harden_worker","runner_create","worker_ready","decode_globals","decode_persistence","unsafe_runner","runtime_construct","seed_runtime","hydrate_state","prepare_source","update_source","compile","evaluate","render","render_operations","snapshot_post","commit_state","persist_state","flush","flush_render","flush_effects","restore_source","command","host_call","snapshot_receive","snapshot_forward","apply_tree","notify","react_snapshot","react_materialize","react_commit","react_snapshot_effect","react_node_error","host_error","fallback","render_result","frame_timer","worker_timer","request_timer","health_probe","idle_probe","visibility","pagehide","pageshow","freeze","resume","frame_probe","event_loop","recovery","quarantine","runner_dispose","worker_stop","frame_dispose","message","error","coverage"],Zt=typeof performance>"u"?Date.now.bind(Date):performance.now.bind(performance),Yn=typeof performance>"u"?0:performance.timeOrigin,qe=Object.getOwnPropertyDescriptor,Zn=Object.getPrototypeOf,to=Error.isError,eo=new Map([[Error.prototype,"Error"],[TypeError.prototype,"TypeError"],[RangeError.prototype,"RangeError"],[SyntaxError.prototype,"SyntaxError"],[ReferenceError.prototype,"ReferenceError"],[URIError.prototype,"URIError"],[EvalError.prototype,"EvalError"],[AggregateError.prototype,"AggregateError"]]);let no=0;const ut=256,oo=32768,Be=512,Vt=32,Z=64,ro=65536,io=8192,so=12288,te=40,$e=new Set(["iframe_create:returned","iframe_load:received","iframe_resource:received","observer_bootstrap:returned","module_entry:returned","module_load:begin","module_resource:returned","module_load:reported_error","runner_module_script_error","runner_module_csp_enforced","runner_module_exception","frame_ready:sent","frame_ready:received","worker_create:begin","worker_create:returned","worker_control:sent","worker_control:received","worker_module:returned","harden_worker:begin","harden_worker:returned","runner_create:sent","runner_create:received","runner_create:begin","runner_create:returned","worker_ready:sent","worker_ready:received","snapshot_post:sent","snapshot_receive:received","host_ingress_lifecycle","host_ingress_snapshot","snapshot_forward:sent","apply_tree:returned","request_timer:fired","worker_timer:fired","frame_timer:fired"]);function Ke(a){if(a.operation==="module_load"&&a.reason==="runner_module_exception")return"runner_module_exception";if(a.fault||a.phase==="threw")return"first_fault";if(a.operation==="module_load"&&a.phase==="reported_error"){if(a.reason==="runner_module_script_error")return"runner_module_script_error";if(a.reason==="csp_violation"&&a.cspResource==="runner_module"&&a.cspDisposition==="enforce")return"runner_module_csp_enforced"}if(a.operation==="quarantine"||a.operation==="error")return a.operation;if(a.operation==="worker_control"&&(a.reason==="execution_started"||a.reason==="execution_finished"))return"worker_execution";if(a.realm==="host"&&a.operation==="message"&&a.phase==="received"&&(a.command==="snapshot"||a.command==="lifecycle"))return\`host_ingress_\${a.command}\`;const t=\`\${a.operation}:\${a.phase}\`;if($e.has(t))return t;if(a.operation==="health_probe"&&a.reason?.startsWith("target_"))return"worker_target";if(a.operation==="health_probe")return a.phase==="fired"?"health_probe_expired":"health_probe";if(a.operation==="recovery")return"recovery";if(a.operation==="event_loop")return"event_loop";if(["visibility","pagehide","pageshow","freeze","resume"].includes(a.operation))return"lifecycle";if(a.operation==="frame_probe")return"frame_probe";if(a.operation==="module_load"&&a.reason==="document_state")return"document_state";if(a.operation==="module_resource")return"module_resource";if(a.operation!=="message"&&a.operation!=="coverage")return"last_progress"}function Je(a){return[a.realm,a.documentBootId,a.realm==="worker"?a.workerGeneration:void 0,a.sourceId].join(":")}const ao=new Set(Xn),lo=new Set(["begin","returned","threw","reported_error","sent","received","acknowledged","resolved","rejected","armed","paused","rearmed","cancelled","fired","ignored","decision"]),uo=new Set(["initialize","__dil_initialize","createRunner","disposeRunner","initializeControl","registerMessengerTransport","unregisterMessengerTransport","setCompiledDil","setData","setStateSnapshot","trigger","invokeGlobal","invokeMessengerCommand","snapshot","lifecycle","messengerEvent"]),co=["workerGeneration","sessionGeneration","startupAttempt","snapshotVersion","renderPass","spanId","parentSpanId","count","pendingRequests","pendingHostCalls","peers","budgetMs","deadlineMs","elapsedMs","httpStatus","resourceStartMs","resourceEndMs","navigationResponseEndMs","navigationDomInteractiveMs"],po=["triggerAcknowledged","workerReadySent","hidden","persisted","acknowledged","replaySafe","hasSuccessfulSnapshot","treePresent","hasError","moduleEvaluationSupported"],_o=["documentBootId","artifactId","runnerId","activeRunnerId","resourceArtifactId","triggerRunnerId","triggerRequestId","requestId","relatedRequestId"],Qe=/^[a-zA-Z0-9_-]{1,96}$/;function ee(a){const t={};for(const n of co){const r=a[n];typeof r=="number"&&Number.isFinite(r)&&r>=(n==="httpStatus"?100:0)&&(n!=="httpStatus"||r<=599)&&(t[n]=r)}for(const n of po)typeof a[n]=="boolean"&&(t[n]=a[n]);for(const n of _o){const r=a[n];typeof r=="string"&&Qe.test(r)&&(t[n]=r)}for(const n of["command","triggerCommand"]){const r=a[n];r&&uo.has(r)&&(t[n]=r)}return(a.documentReadyState==="loading"||a.documentReadyState==="interactive"||a.documentReadyState==="complete")&&(t.documentReadyState=a.documentReadyState),a.cspDirective&&["script-src","script-src-elem","script-src-attr","worker-src","other"].includes(a.cspDirective)&&(t.cspDirective=a.cspDirective),(a.cspDisposition==="enforce"||a.cspDisposition==="report")&&(t.cspDisposition=a.cspDisposition),a.cspResource&&["runner_module","inline","eval","unknown"].includes(a.cspResource)&&(t.cspResource=a.cspResource),typeof a.reason=="string"&&/^[a-z_]{1,48}$/.test(a.reason)&&(t.reason=a.reason),t}function mo(a){const t=[];let n="unknown";try{if(to?.(a)){n=eo.get(Zn(a))??n;const r=qe(a,"name")?.value;typeof r=="string"&&/^(Error|TypeError|RangeError|SyntaxError|ReferenceError|URIError|EvalError|AggregateError)$/.test(r)&&(n=r);const l=qe(a,"stack")?.value;if(typeof l=="string"){n=/^(Error|TypeError|RangeError|SyntaxError|ReferenceError|URIError|EvalError|AggregateError)(?=:|$)/.exec(l)?.[1]??n;for(const o of l.slice(0,4096).split(\`
\`).slice(1,17)){const u=/:(\\d{1,7}):(\\d{1,7})\\)?$/.exec(o.trim());u&&t.push({line:Number(u[1]),column:Number(u[2])})}}}}catch{}return Object.freeze({name:n,locations:Object.freeze(t.map(r=>Object.freeze(r))),stackUnavailable:t.length===0})}function Tt(a){if(a===null||typeof a!="object")return;const t=a;if(!["host","bootstrap","iframe","worker"].includes(t.realm)||typeof t.operation!="string"||!ao.has(t.operation)||typeof t.phase!="string"||!lo.has(t.phase)||!Number.isSafeInteger(t.sequence)||t.sequence<0||typeof t.localTimeMs!="number"||!Number.isFinite(t.localTimeMs)||typeof t.timeOriginMs!="number"||!Number.isFinite(t.timeOriginMs))return;const n={...ee(t),realm:t.realm,sequence:t.sequence,localTimeMs:t.localTimeMs,timeOriginMs:t.timeOriginMs,operation:t.operation,phase:t.phase};Number.isSafeInteger(t.sourceId)&&t.sourceId>=0&&(n.sourceId=t.sourceId);const r=t.fault;if(r&&typeof r.name=="string"&&/^(unknown|Error|TypeError|RangeError|SyntaxError|ReferenceError|URIError|EvalError|AggregateError)$/.test(r.name)&&Array.isArray(r.locations)&&r.locations.length<=16){const l=r.locations.filter(o=>o&&Number.isSafeInteger(o.line)&&o.line>=0&&o.line<1e7&&Number.isSafeInteger(o.column)&&o.column>=0&&o.column<1e7).map(({line:o,column:u})=>Object.freeze({line:o,column:u}));n.fault=Object.freeze({name:r.name,locations:Object.freeze(l),stackUnavailable:l.length===0})}return n}function tt(a){return[a.realm,a.documentBootId,a.workerGeneration,a.sourceId,a.runnerId,a.sessionGeneration,a.startupAttempt].join(":")}function ne(a,t,n){const r=n?t.hostReceivedAtMs:Zt();return{...a,...typeof r=="number"&&Number.isFinite(r)&&r>=0?{hostReceivedAtMs:r}:{}}}class fo{context;now=Zt;realm;emit;sequence=0;sourceId=++no;span=0;parentSpanId;dropped=0;bytes=0;faultCount=0;emitted=0;open=new Map;completedSpans=new Map;observedRealms=new Set;upstreamDropped=new Map;upstreamTruncated=!1;entries=[];sequences=new Map;progress=new Map;progressSources=new Map;retainedRunners=new Map;progressBytes=0;progressDropped=0;progressLoss=new Map;constructor(t){this.realm=t.realm,this.context=Object.freeze(ee(t.context??{})),this.emit=t.emit}record(t,n,r={}){try{const l={...this.context,...ee(r),realm:this.realm,sequence:++this.sequence,sourceId:this.sourceId,localTimeMs:Zt(),timeOriginMs:Yn,operation:t,phase:n};r.error!==void 0&&this.faultCount<4&&(l.fault=mo(r.error),this.faultCount++),this.retain(l,"complete"),this.forward(l),this.sequence===Be&&this.record("coverage","ignored",{reason:"overflow"})}catch{this.dropped++}}trace(t,n,r={}){const l=++this.span,o=this.parentSpanId,u={...r,spanId:l,parentSpanId:o};this.record(t,"begin",u),this.parentSpanId=l;try{const d=n();return this.record(t,"returned",u),d}catch(d){throw this.fault(t,d,u),d}finally{this.parentSpanId=o}}fault(t,n,r={}){this.record(t,"threw",{...r,error:n})}ingest(t,n=!1,r=!1){try{const l=Tt(t);if(!l){this.dropped++;return}const o=tt(l);if(l.sequence<=(this.sequences.get(o)??-1))return;this.sequences.set(o,l.sequence),this.sequences.size>32&&this.sequences.delete(this.sequences.keys().next().value);const u=ne(l,t,r),d=Je(l),s=this.progressSources.get(d),m=s?s.complete&&l.sequence===s.sequence+1&&l.operation!=="coverage":l.sequence===1&&l.operation!=="coverage";if(l.sequence>(s?.sequence??-1)&&(this.progressSources.set(d,{sequence:l.sequence,complete:m}),this.progressSources.size>Z&&this.progressSources.delete(this.progressSources.keys().next().value),!m))for(const h of this.progress.values())Je(h.slots.values().next().value)===d&&(h.coverage="interrupted");this.retain(u,m?"complete":"interrupted"),n&&this.forward(l)}catch{this.dropped++}}mergeSnapshot(t,n=!1,r,l="merge"){try{if(t===null||typeof t!="object")return this.dropped++,!1;const o=t;if(o.schemaVersion!==1||!Array.isArray(o.records)||!Array.isArray(o.openOperations))return this.dropped++,!1;if(r){const e=f=>f!==null&&typeof f=="object"&&Object.entries(r).every(([g,w])=>w===void 0||f[g]===w),c=[...o.records.slice(0,ut),...o.openOperations.slice(0,Vt)];if(Array.isArray(o.progress))for(const f of o.progress.slice(0,Z)){if(!f||!Array.isArray(f.records))return this.markProgressDropped(r.runnerId),!1;for(const g of f.records.slice(0,te))c.push({...f.context,...g})}if(!c.every(e))return this.markProgressDropped(r.runnerId),!1}if(Array.isArray(o.observedRealms))for(const e of o.observedRealms.slice(0,4))(e==="host"||e==="bootstrap"||e==="iframe"||e==="worker")&&this.observedRealms.add(e);for(const e of o.records.slice(0,ut))this.ingest(e,!1,n);l==="replace"&&Array.isArray(o.progress)&&this.resetDocumentProgress();for(const e of o.openOperations.slice(0,Vt)){const c=Tt(e);if(!c||c.phase!=="begin"||c.spanId===void 0){this.dropped++;continue}this.observeSpan(ne(c,e,n))}let u=!0;if(Array.isArray(o.progress)){const e=this.progressDropped;for(const c of o.progress.slice(0,Z))this.mergeProgress(c,n);o.progress.length>Z&&this.markProgressDropped(),u=e===this.progressDropped}if(Number.isSafeInteger(o.progressDropped)&&o.progressDropped>0){this.progressDropped=Math.max(this.progressDropped,o.progressDropped);const e=typeof o.runnerId=="string"&&Qe.test(o.runnerId)&&(this.progressLoss.has(o.runnerId)||this.progressLoss.size<Z)?o.runnerId:"";this.progressLoss.set(e,Math.max(this.progressLoss.get(e)??0,o.progressDropped))}const d=Tt(o.records[0]??o.openOperations[0]),s=d?tt(d):"empty",m=this.upstreamDropped.get(s)??0,h=typeof o.dropped=="number"&&Number.isSafeInteger(o.dropped)&&o.dropped>=0?o.dropped:0,x=Math.max(0,o.records.length-ut)+Math.max(0,o.openOperations.length-Vt),_=Math.min(Number.MAX_SAFE_INTEGER,h+x);return this.dropped=Math.min(Number.MAX_SAFE_INTEGER,this.dropped+Math.max(0,_-m)),this.upstreamDropped.set(s,Math.max(m,_)),this.upstreamDropped.size>32&&this.upstreamDropped.delete(this.upstreamDropped.keys().next().value),this.upstreamTruncated||=o.truncated===!0||x>0,u}catch{return this.dropped++,!1}}resetDocumentProgress(){this.progress.clear(),this.progressSources.clear(),this.progressLoss.clear(),this.progressBytes=0,this.progressDropped=0,this.open.clear(),this.completedSpans.clear()}retainRunner(t){if(!this.retainedRunners.has(t)&&this.retainedRunners.size>=Z)return this.markProgressDropped(t),()=>{};this.retainedRunners.set(t,(this.retainedRunners.get(t)??0)+1);let n=!1;return()=>{if(n)return;n=!0;const r=(this.retainedRunners.get(t)??1)-1;r?this.retainedRunners.set(t,r):this.retainedRunners.delete(t)}}snapshot(t={}){const n=[...this.progress.values()].filter(d=>{const s=d.slots.values().next().value;return!t.runnerId||!s.runnerId||s.runnerId===t.runnerId}).map(d=>{const s=[...d.slots.values()],{realm:m,sourceId:h,timeOriginMs:x,documentBootId:_,artifactId:e,runnerId:c,workerGeneration:f,sessionGeneration:g,startupAttempt:w}=s[0];return{context:{realm:m,sourceId:h,timeOriginMs:x,documentBootId:_,artifactId:e,runnerId:c,workerGeneration:f,sessionGeneration:g,startupAttempt:w},records:s.map(({realm:P,sourceId:O,timeOriginMs:V,documentBootId:D,artifactId:i,runnerId:p,workerGeneration:y,sessionGeneration:b,startupAttempt:E,...k})=>k),lastSequence:d.lastSequence,coverage:d.coverage}}),r=t.runnerId?[...ho(n,t.runnerId,this.context.documentBootId)].sort((d,s)=>+(s.context.runnerId===t.runnerId)-+(d.context.runnerId===t.runnerId)):n,l=[];let o=2,u=0;for(const d of r){const s=JSON.stringify(JSON.stringify(d)).length+1;if(o+s>io){u++;continue}o+=s,l.push(Object.freeze({...d,context:Object.freeze(d.context),records:Object.freeze(d.records.map(m=>Object.freeze(m)))}))}return Object.freeze({schemaVersion:1,observedRealms:Object.freeze([...this.observedRealms]),documentBootId:this.context.documentBootId,runnerId:t.runnerId,progress:Object.freeze(l),progressDropped:u+(t.runnerId?(this.progressLoss.get(t.runnerId)??0)+(this.progressLoss.get("")??0):this.progressDropped),records:Object.freeze(t.records===!1?[]:this.entries.map(d=>d.record)),openOperations:Object.freeze(t.records===!1?[]:[...this.open.values()]),dropped:this.dropped,truncated:this.dropped>0||this.upstreamTruncated})}markProgressDropped(t){this.progressDropped++;const n=t&&(this.progressLoss.has(t)||this.progressLoss.size<Z)?t:"";this.progressLoss.set(n,(this.progressLoss.get(n)??0)+1)}observeProgress(t,n,r){const l=Ke(t);if(!l)return;const o=tt(t);let u=this.progress.get(o);const d=u?.slots.get(l);if(u&&(u.lastSequence=Math.max(u.lastSequence,t.sequence)),d&&l==="event_loop"&&(d.elapsedMs??0)-(d.budgetMs??0)>=(t.elapsedMs??0)-(t.budgetMs??0)||d&&(l==="first_fault"||$e.has(l)?d.sequence<=t.sequence:d.sequence>=t.sequence))return;r??=JSON.stringify(t).length;const s=r-(d?JSON.stringify(d).length:0);for(;!u&&this.progress.size>=Z||this.progressBytes+s>ro;){const m=[...this.progress].find(([h,x])=>{const _=x.slots.values().next().value.runnerId;return h!==o&&_&&!this.retainedRunners.has(_)});if(!m){this.markProgressDropped(t.runnerId),u&&(u.coverage="interrupted");return}this.progressBytes-=m[1].bytes,this.progress.delete(m[0]),this.markProgressDropped(m[1].slots.values().next().value.runnerId)}if(u||(u={slots:new Map,lastSequence:0,coverage:n,bytes:0},this.progress.set(o,u)),u.bytes+s>so||!d&&u.slots.size>=te){this.markProgressDropped(t.runnerId),u.coverage="interrupted";return}u.slots.set(l,Object.freeze(t)),u.lastSequence=Math.max(u.lastSequence,t.sequence),n!=="complete"&&(u.coverage=n),u.bytes+=s,this.progressBytes+=s}mergeProgress(t,n){if(!t||typeof t!="object"){this.markProgressDropped();return}const r=t;if(!Array.isArray(r.records)||r.records.length===0||r.records.length>te||!Number.isSafeInteger(r.lastSequence)||r.lastSequence<0||!["complete","interrupted","unavailable"].includes(r.coverage??"")){this.markProgressDropped();return}const l=r.records.map(m=>Tt({...r.context,...m})),o=l[0];if(!o||l.some(m=>!m||tt(m)!==tt(o)||m.sequence>r.lastSequence||!Ke(m))){this.markProgressDropped();return}const u=this.progress.get(tt(o));if(u&&r.lastSequence<u.lastSequence)return;const d=this.progressDropped;for(let m=0;m<l.length;m++)this.observeProgress(ne(l[m],r.records[m],n),r.coverage);const s=this.progress.get(tt(o));s&&(s.lastSequence=Math.max(s.lastSequence,r.lastSequence),s.coverage=this.progressDropped===d?r.coverage:"interrupted")}observeSpan(t){if(t.spanId!==void 0){const n=\`\${tt(t)}:\${t.spanId}\`;if(t.phase==="begin"){if((this.completedSpans.get(n)??-1)>=t.sequence||(this.open.get(n)?.sequence??-1)>=t.sequence)return;this.open.has(n)||this.open.size<Vt?this.open.set(n,Object.freeze(t)):this.dropped++}else(t.phase==="returned"||t.phase==="threw")&&(this.open.delete(n),this.completedSpans.set(n,t.sequence),this.completedSpans.size>ut&&this.completedSpans.delete(this.completedSpans.keys().next().value))}}retain(t,n){const r=JSON.stringify(t).length;this.observeProgress(t,n,r),this.observedRealms.add(t.realm),this.observeSpan(t);const l=this.entries.length<8||t.fault!==void 0&&!this.entries.some(o=>o.record.fault);for(;this.entries.length>=ut||this.bytes+r>oo;){const o=this.entries.findIndex(u=>!u.pinned);if(o<0){this.dropped++;return}this.bytes-=this.entries[o].bytes,this.entries.splice(o,1),this.dropped++}this.entries.push({record:Object.freeze(t),bytes:r,pinned:l}),this.bytes+=r}forward(t){if(this.emit){if(this.emitted++>=Be+1){this.dropped++;return}try{this.emit(t)}catch{this.dropped++}}}}function ho(a,t,n){const r=a.filter(({context:s})=>(!t||!s.runnerId||s.runnerId===t)&&(!n||!s.documentBootId||s.documentBootId===n)),l=r.map(s=>s.context).filter(s=>s.runnerId===t),o=Math.max(-1,...l.map(s=>s.workerGeneration??-1)),u=Math.max(-1,...l.filter(s=>s.workerGeneration===o).map(s=>s.sessionGeneration??-1)),d=Math.max(-1,...l.filter(s=>s.workerGeneration===o&&s.sessionGeneration===u).map(s=>s.startupAttempt??-1));return r.filter(({context:s})=>(s.workerGeneration===void 0||s.workerGeneration===o)&&(s.sessionGeneration===void 0||s.sessionGeneration===u)&&(s.startupAttempt===void 0||s.startupAttempt===d))}const go=performance.now(),at=new Map;let lt,Q;dt(globalThis,"message",Xe),dt(globalThis,"error",a=>{const t=a;Q?.record("error","reported_error",{error:t.error??t.message,reason:"uncaught_error"})}),dt(globalThis,"unhandledrejection",a=>{Q?.record("error","rejected",{error:a.reason,reason:"unhandled_rejection"})});function Xe(a){if(lt||a.ports.length!==1||!Vn(a.data))return;Fe(globalThis,"message",Xe);const t=a.ports[0];Q=Ye(a.data.diagnostics,t),Q?.record("worker_module","returned",{elapsedMs:Q.now()-go}),Q?.record("worker_control","received");try{X(Q,"harden_worker",yo),dt(t,"message",vo),t.start(),lt=t,Q?.record("worker_control","returned")}catch(n){throw Q?.fault("worker_control",n),t.close(),n}}function yo(){globalThis.RegExp=kn.createSandboxRegExp(RegExp);const a=()=>{throw new Error("DIL policy blocked dynamic code evaluation")},t=[Function.prototype,Object.getPrototypeOf(async function(){}),Object.getPrototypeOf(function*(){}),Object.getPrototypeOf(async function*(){})];for(const l of t)Object.defineProperty(l,"constructor",{configurable:!1,enumerable:!1,value:a,writable:!1});globalThis.eval=a;const n=Object.getPrototypeOf(globalThis),r=EventTarget.prototype;for(let l=n;l!==null&&l!==r;l=Object.getPrototypeOf(l))for(const o of Reflect.ownKeys(l)){if(o==="constructor"||Reflect.deleteProperty(l,o))continue;const u=Reflect.get(l,o,globalThis);if(typeof u=="function"||typeof u=="object"&&u!==null)throw new Error(\`Failed to remove browser capability "\${String(o)}"\`)}for(const l of Reflect.ownKeys(r))l!=="constructor"&&Object.defineProperty(n,l,{configurable:!0,value:void 0});Dn.hardenSandboxGlobal(globalThis,!0)}function vo(a){if(bo(a.data)){const l=a.data.runnerId,o=typeof l=="string"&&l.length<=128?at.get(l):void 0,u=o?.diagnostics;lt?.postMessage({__oaiDilWorker:!0,kind:"healthy",requestId:a.data.requestId,...typeof l=="string"&&l.length<=128?{target:{...Q?.context,...u?.context,runnerId:l,present:o!==void 0,workerReadySent:o?.workerReadySent}}:{},...u?{diagnostics:u.snapshot({runnerId:l,records:!1})}:{}}),Q?.record("health_probe","sent",{requestId:a.data.requestId});return}if(Fn(a.data)){const l=at.get(a.data.runnerId);l&&(at.delete(a.data.runnerId),ct(a.data.runnerId,()=>X(l.diagnostics,"runner_dispose",l.dispose)));return}if(a.ports.length!==1||!Wn(a.data))return;const t=a.data,n=at.get(t.runnerId);n&&(at.delete(t.runnerId),ct(t.runnerId,()=>X(n.diagnostics,"runner_dispose",n.dispose)));const r=Ye(t.diagnosticContext,lt);r?.record("runner_create","received"),ct(t.runnerId,()=>{X(r,"runner_create",()=>Io(t,a.ports[0],r))})}function Io(a,t,n){const r=new Map;let l,o=null,u=0;const s=Object.fromEntries(Object.entries({registerMessengerTransport:({channelId:e})=>{r.get(e)?.();const c={command:(f,g)=>m.commands.invokeMessengerCommand({channelId:e,data:g,type:f}),event:(f,g)=>{m.emit("messengerEvent",{channelId:e,data:g,type:f})}};r.set(e,_().registerDilMessengerTransport(e,c))},setCompiledDil:({compiledDil:e,data:c,dilComponents:f})=>{_().updateSource(e,c,f)},setData:({data:e})=>{_().setData(e)},setStateSnapshot:({snapshot:e,widgetId:c})=>{_().setStateSnapshot(e,c)},trigger:e=>{const c=l;l=e.interactionCapability;try{const f=Number(e.id.slice(9));if(!Number.isSafeInteger(f))throw new Error(\`Unknown DIL callback "\${e.id}"\`);return _().invokeFunction(f,e.args,e.propagateErrors)}finally{l=c}},unregisterMessengerTransport:({channelId:e})=>{r.get(e)?.(),r.delete(e)}}).map(([e,c])=>[e,f=>ct(a.runnerId,()=>c(f))])),m=new On({handlers:{...s,[Mn]:()=>{}},port:t,diagnostics:n}),h=Qn(a,m,()=>l),x={diagnostics:n,workerReadySent:!1,dispose(){h.dispose();for(const e of r.values())e();r.clear(),o?.dispose(),m.dispose()}};at.set(a.runnerId,x),m.emit("lifecycle",{kind:"worker_ready"}),x.workerReadySent=!0,n?.record("worker_ready","sent");try{const e=X(n,"decode_globals",()=>Ve(a.globals,(g,w)=>m.commands.invokeGlobal({args:w,id:g,...l==null?{}:{interactionCapability:l}}))),c=a.statePersistence,f=c?X(n,"decode_persistence",()=>Ve({persistState:c.persistState},(g,w)=>m.commands.invokeGlobal({args:w,id:g})).persistState):void 0;if(c&&typeof f!="function")throw new Error("DIL state persistence is not available");o=X(n,"unsafe_runner",()=>new Ln(a.compiledDil,{...h.options,data:a.data,dilComponents:a.dilComponents,globals:e,onError:g=>{u+=1,n?.record("error","reported_error",{error:g,snapshotVersion:u}),X(n,"snapshot_post",()=>m.emit("snapshot",{error:Xt(g),version:u})),n?.record("snapshot_post","sent",{snapshotVersion:u,hasError:!0})},onOperations:g=>{u+=1,X(n,"snapshot_post",()=>m.emit("snapshot",{error:null,operations:g,version:u})),n?.record("snapshot_post","sent",{snapshotVersion:u,hasError:!1})},...n?{trace:(g,w)=>{const P=xo(g);return P?n.trace(P,w):w()}}:{},runWithExecutionAttribution:g=>ct(a.runnerId,g),...c&&typeof f=="function"?{onStateChange:(g,w)=>{Promise.resolve(X(n,"persist_state",()=>f(g,w))).catch(()=>{})},stateSnapshot:c.snapshot,widgetStateSnapshots:c.widgetSnapshots}:{}}))}catch(e){n?.record("runner_create","reported_error",{error:e}),m.emit("lifecycle",{kind:"failure",stage:"runtime_error"}),X(n,"snapshot_post",()=>m.emit("snapshot",{error:Xt(e),version:u+1})),n?.record("snapshot_post","sent",{snapshotVersion:u+1,hasError:!0})}function _(){if(!o)throw new Error("DIL frame runner is not initialized");return o}}function ct(a,t){lt?.postMessage({__oaiDilWorker:!0,kind:"executionStarted",runnerId:a});try{return t()}finally{lt?.postMessage({__oaiDilWorker:!0,kind:"executionFinished",runnerId:a})}}function bo(a){return a!=null&&typeof a=="object"&&a.__oaiDilWorker===!0&&a.kind==="healthCheck"&&typeof a.requestId=="string"}function Ye(a,t){if(!(!a||!t))return new fo({realm:"worker",context:a,emit:n=>{t.postMessage({__oaiDilWorker:!0,kind:"diagnostic",record:n})}})}function X(a,t,n){return a?a.trace(t,n):n()}function xo(a){switch(a){case"DILRenderer.compileJS":return"compile";case"DILRenderer.evaluateJS":return"evaluate";case"DILRenderer.render":return"render";case"runtime_construct":case"seed_runtime":case"hydrate_state":case"prepare_source":case"update_source":case"render_operations":case"commit_state":case"flush":case"flush_render":case"flush_effects":case"restore_source":return a;default:return}}})();
`,F=typeof self<"u"&&self.Blob&&new Blob(["(self.URL || self.webkitURL).revokeObjectURL(self.location.href);",B],{type:"text/javascript;charset=utf-8"});function Re(r){let e;try{if(e=F&&(self.URL||self.webkitURL).createObjectURL(F),!e)throw"";const t=new Worker(e,{name:r?.name});return t.addEventListener("error",()=>{(self.URL||self.webkitURL).revokeObjectURL(e)}),t}catch{return new Worker("data:text/javascript;charset=utf-8,"+encodeURIComponent(B),{name:r?.name})}}function Ce(){return new Re}const we=/^runner-([A-Za-z0-9_-]{8,64})\.js$/.exec(new URL(import.meta.url).pathname.split("/").at(-1)??"")?.[1],ke=new URLSearchParams(location.hash.slice(1)).get("dil-diagnostics")==="1",g=ke?new fe({realm:"iframe",context:{artifactId:we,documentBootId:window.__oaiDilBootstrap?.documentBootId??`${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}`},emit:r=>window.parent.postMessage({__oaiDilFrame:!0,kind:"diagnostic",protocolVersion:b,record:r},"*")}):void 0;g?.record("module_entry","returned");const S=new Ie({createWorker:Ce,diagnostics:g});window.addEventListener("message",Se);window.addEventListener("pagehide",r=>{r.persisted||S.dispose()});const Ee={__oaiDilFrame:!0,kind:"ready",protocolVersion:b};window.parent.postMessage(Ee,"*");g?.record("frame_ready","sent");function Se(r){if(r.source!==window.parent)return;const e=r.data;if(g&&e&&typeof e=="object"&&"__oaiDilFrame"in e&&e.__oaiDilFrame===!0&&"kind"in e&&e.kind==="diagnosticProbe"&&"protocolVersion"in e&&e.protocolVersion===b&&"requestId"in e&&typeof e.requestId=="string"&&e.requestId.length<=128){const t="runnerId"in e&&typeof e.runnerId=="string"&&e.runnerId.length<=128?e.runnerId:void 0;g.record("frame_probe","received",{requestId:e.requestId,runnerId:t}),S.probeTarget(t,()=>window.parent.postMessage({__oaiDilFrame:!0,kind:"diagnosticSnapshot",protocolVersion:b,requestId:e.requestId,documentBootId:g.context.documentBootId,diagnostics:g.snapshot({runnerId:t,records:!1})},"*"));return}if(Q(r.data)){if(r.ports.length!==1)return;g?.record("runner_create","received",{runnerId:r.data.runnerId}),S.createRunner(r.data,r.ports[0]);return}X(r.data)&&(g?.record("runner_dispose","received",{runnerId:r.data.runnerId}),S.disposeRunner(r.data.runnerId))}
