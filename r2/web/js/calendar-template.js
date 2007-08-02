function cookieValueHandler(event) {
    var src = Event.element(event)
    Cookies.set(src.name, src.value, 365)
    window.location.reload()
}

function registerSpecialInputs() {
    $$("input").each(function(input) {
        if (input.getAttribute("special") == "cookie") {
            Event.observe(input, "click", cookieValueHandler)
            if (input.type == "radio" && Cookies.get(input.name) == input.value) {
                input.checked = true
            }
        }
    });
}

Event.observe(window, 'load', registerSpecialInputs)
