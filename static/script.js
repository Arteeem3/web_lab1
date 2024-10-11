let x, y, r;

function setupButtons(className) {
    document.querySelectorAll('.' + className + ' input[type="button"]').forEach(button => {
        button.addEventListener('click', function () {
            document.querySelectorAll('.' + className + ' input[type="button"]').forEach(btn => {
                btn.classList.remove('active');
            });
            this.classList.add('active');
            updateValues();
            });
        })
    }

    function setupY() {
        y = document.getElementById("yInput").value.trim().replace(',','.');
    }



    function updateValues() {
        x = document.querySelector('.xButtons input.active')?.value;
        r = document.querySelector('.rButtons input.active')?.value;
    }

    function isValidY() {
    if (y === undefined || y === null || y === "") {
        alert('Введите Y');
        return false;
    } else if (isNaN(parseFloat(y)) || !isFinite(y)) {
        alert('Y должен быть числом');
        return false;
    } else if (parseFloat(y) < -3 || parseFloat(y) > 5) {
        alert('Y должен быть в диапазоне от -3 до 5');
        return false;
    } else return true;
}
setupButtons('xButtons');
setupY();
setupButtons('rButtons');
document.getElementById("submit").addEventListener("click", function(event) {
    event.preventDefault(); // Prevent form submission or page reload

    // Validate Y on the client side
    isValidY();


    const x = parseFloat(x);
    const y = parseFloat(y);
    const r = parseFloat(r);

    // Prepare data for sending to the server
    const data = { x: x, y: y, r: r };

    // Send POST request to the server
    fetch("http://localhost:21038/fcgi-bin/app.jar", {
        method: "POST",
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(resp => {
            if(!resp.ok) { // Check if any error occurred
                console.log('something is wrong with the response...');
                return resp.text().then(text => { throw new Error(text) });
            }
            else {
                console.log('success');
                return resp.json(); // Convert to JSON
            }
        })
        .then(result => { // Handle the data from the response
            console.log('result is: ' + JSON.stringify(result, null, 2)); // Pretty-print the JSON result
            addResultToTable(x, y, r, result.response.hit, result.currentTime, result.elapsedTime);
        })
        .catch(error => {
            console.error("catch error:", error);
        });
});




function addResultToTable(x, y, r, hit, currentTime, elapsedTime) {
    const resultBody = document.getElementById("result-body");
    const newRow = document.createElement("tr");

    const xCell = document.createElement("td");
    xCell.textContent = x;

    const yCell = document.createElement("td");
    yCell.textContent = y;

    const rCell = document.createElement("td");
    rCell.textContent = r;

    const resultCell = document.createElement("td");
    resultCell.textContent = hit ? "Hit" : "Miss";

    const currentTimeCell = document.createElement("td");
    currentTimeCell.textContent = currentTime;

    const elapsedTimeCell = document.createElement("td");
    elapsedTimeCell.textContent = elapsedTime + " ms";

    newRow.appendChild(xCell);
    newRow.appendChild(yCell);
    newRow.appendChild(rCell);
    newRow.appendChild(resultCell);
    newRow.appendChild(currentTimeCell);
    newRow.appendChild(elapsedTimeCell);

    resultBody.appendChild(newRow);
}

