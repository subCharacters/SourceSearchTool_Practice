// 선택된 Repository 목록을 담는 배열
let selectedRepositories = [];
let currentPage = 0;  // 현재 페이지 번호
let totalCnt = 0;
let displayedCnt = 0;

// Repository 항목 클릭 시 처리하는 함수
function toggleRepository(repoElement) {
    const repoName = repoElement.textContent.trim();

    // 선택된 상태라면 해제
    if (selectedRepositories.includes(repoName)) {
        // 배열에서 제거
        selectedRepositories = selectedRepositories.filter(name => name !== repoName);

        // 선택한 Repository 목록에서 태그 삭제
        removeSelectedTag(repoName);

        // 선택된 스타일 제거
        repoElement.classList.remove("selected");

    } else {
        // 배열에 추가
        selectedRepositories.push(repoName);

        // 선택한 Repository 목록에 태그 추가
        addSelectedTag(repoName);

        // 선택된 스타일 추가
        repoElement.classList.add("selected");
    }

    // 선택된 Repository 목록을 서버로 전송
    sendSelectedRepositoriesToServer();
}

// 선택한 Repository 태그 추가 함수
function addSelectedTag(repoName) {
    const tagContainer = document.querySelector('.selected-repository');
    const tagElement = document.createElement('span');
    tagElement.classList.add('tag');
    tagElement.textContent = repoName;

    // 삭제 버튼 추가
    const removeButton = document.createElement('button');
    removeButton.textContent = "x";
    removeButton.onclick = function() {
        removeTag(repoName);
    };

    tagElement.appendChild(removeButton);
    tagContainer.appendChild(tagElement);

    // 선택된 영역을 자동으로 스크롤
    tagContainer.scrollTop = tagContainer.scrollHeight;
}

// 선택한 Repository 태그 삭제 함수
function removeSelectedTag(repoName) {
    const tags = document.querySelectorAll('.selected-repository .tag');
    tags.forEach(tag => {
        if (tag.textContent.includes(repoName)) {
            tag.remove();
        }
    });
}

// 태그에서 제거 버튼 클릭 시 처리
function removeTag(repoName) {
    const repoElements = document.querySelectorAll('.sidebar .item');
    repoElements.forEach(repoElement => {
        if (repoElement.textContent.trim() === repoName) {
            repoElement.classList.remove('selected');
        }
    });

    // 배열에서 제거
    selectedRepositories = selectedRepositories.filter(name => name !== repoName);

    // 태그 삭제
    removeSelectedTag(repoName);

    // 선택된 Repository 목록을 서버로 전송
    sendSelectedRepositoriesToServer();
}

// 선택된 Repository 목록을 서버로 전송하는 함수 (예시로 fetch 사용)
function sendSelectedRepositoriesToServer() {
    console.log(selectedRepositories);
}

// ================================================================
// 검색 버튼을 눌렀을 때 호출되는 함수
function search() {
    // 검색어 입력 값 가져오기
    const searchWord = document.getElementById('searchWord').value;
    const searchType = document.getElementById('searchType').value;
    const caseSensitive = document.getElementById('caseSensitive').value;
    const fileExtension = document.getElementById('fileExtension').value;
    const searchWithinResults = document.getElementById('searchWithinResults').checked;

    function reSearchResults() {

        const tableRows = document.querySelectorAll('.search-results tr');
        let filteredResults = [];

        tableRows.forEach(row => {
            const repositoryName = row.cells[0].textContent.trim();
            const fileName = row.cells[1].textContent.trim();
            const lineContent = row.cells[3].textContent.trim();

            // 선택된 리포지토리 매칭
            let repositoryMatch = false;
            selectedRepositories.forEach(repoTag => {
                if (repositoryName === repoTag.trim()) {
                    repositoryMatch = true;
                }
            });

            if (selectedRepositories.length == 0) {
                repositoryMatch = true;
            }

            // 파일 확장자 매칭 (확장자가 없으면 매칭하지 않음)
            let extensionMatch = fileExtension === 'none' || fileName.endsWith('.' + fileExtension);

            let isCase = caseSensitive === 'case';
            // 대소문자 구분 및 단어 매칭
            let contentMatch = isCase
                ? lineContent.includes(searchWord)
                : lineContent.toLowerCase().includes(searchWord.toLowerCase());

            // 세 조건이 모두 만족할 경우 결과에 추가
            if (repositoryMatch && extensionMatch && contentMatch) {
                filteredResults.push(row.outerHTML); // 해당 행을 추가
            }
        });

        // 새로운 창에 필터링된 결과를 출력
        const newWindow = window.open('', '_blank');
        newWindow.document.write(`
        <html>
        <head>
            <title>재검색 결과</title>
            <style>
                table { width: 100%; border-collapse: collapse; }
                th, td { border: 1px solid black; padding: 8px; text-align: left; }
                th { background-color: #f2f2f2; }
            </style>
        </head>
        <body>
            <h1>재검색 결과</h1>
            <table>
                <thead>
                    <tr>
                        <th>리포지토리명</th>
                        <th>파일명</th>
                        <th>라인 수</th>
                        <th>라인 내용</th>
                    </tr>
                </thead>
                <tbody>
                    ${filteredResults.join('')}
                </tbody>
            </table>
        </body>
        </html>
    `);
        newWindow.document.close();
    }

    if (searchWithinResults == true && searchType == 'line') {
        reSearchResults();
        return;
    }

    // 검색어가 비어 있으면 처리 중단
    if (!searchWord) {
        alert("검색어를 입력해 주세요.");
        return;
    }

    // 검색 시작 시 모달 표시
    showModal();

    // hidden input에 검색 조건 저장
    document.getElementById('hiddenSearchWord').value = searchWord;
    document.getElementById('hiddenSearchType').value = searchType;
    document.getElementById('hiddenCaseSensitive').value = caseSensitive;
    document.getElementById('hiddenFileExtension').value = fileExtension;
    document.getElementById('hiddenRepositoryNames').value = JSON.stringify(selectedRepositories);

    console.log(searchType);
    console.log(caseSensitive);
    console.log(fileExtension);
    console.log(searchWithinResults);

    // 비동기 요청 보내기 (POST)
    fetch('/search', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(
            {
                "searchWord": searchWord,
                "searchType": searchType,
                "caseSensitive": caseSensitive,
                "fileExtension": fileExtension,
                "searchWithinResults": searchWithinResults,
                "repositoryNames": selectedRepositories,
                "lastScoreDocId": 0
            }
        )
    })
        .then(response => response.json()) // JSON 형식의 응답 처리
        .then(data => {
            // 검색 결과를 처리하는 함수 호출
            displaySearchResults(data);
            // 통신 완료 후 모달 숨김
            hideModal();
        })
        .catch(error => {
            console.error(error);
            hideModal(); // 오류 발생 시에도 모달 숨김
            alert("검색 중 에러가 발생했습니다.");
        });
}

// 다음 페이지 버튼 클릭 시 호출
function nextPage() {
    /* TODO 서버로부터 제어
    if (displayedCnt >= totalCnt) {
        alert("마지막 페이지 입니다.");
        return;
    }  // 이미 마지막 페이지라면 동작하지 않음
*/
    const lastScoreDocId = document.getElementById('hiddenLastScoreDocId').value;
    const docScore = document.getElementById('hiddenDocScore').value;
    nextPageSearch(lastScoreDocId, docScore);
}

function nextPageSearch(lastScoreDocId, docScore) {
    const searchWord = document.getElementById('hiddenSearchWord').value;
    const searchType = document.getElementById('hiddenSearchType').value;
    const caseSensitive = document.getElementById('hiddenCaseSensitive').value;
    const fileExtension = document.getElementById('hiddenFileExtension').value;
    const repositoryNames = JSON.parse(document.getElementById('hiddenRepositoryNames').value);

    // 검색 시작 시 모달 표시
    showModal();

    // 비동기 요청 보내기 (POST)
    fetch('/search/next', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            "searchWord": searchWord,
            "searchType": searchType,
            "caseSensitive": caseSensitive,
            "fileExtension": fileExtension,
            "repositoryNames": repositoryNames,
            "lastScoreDocId": lastScoreDocId,  // 현재 페이지 번호
            "docScore": docScore
        })
    })
        .then(response => response.json())  // JSON 형식의 응답 처리
        .then(data => {
            // 검색 결과 표시
            displaySearchResults(data, true);  // page > 1이면 append 방식으로 추가
            // 통신 완료 후 모달 숨김
            hideModal();
        })
        .catch(error => {
            console.error(error);
            hideModal(); // 오류 발생 시에도 모달 숨김
            alert("검색 중 에러가 발생했습니다.");
        });
}

// 검색 결과를 표시하는 함수
function displaySearchResults(data, append = false) {
    const resultsContainer = document.querySelector('.search-results');

    // 기존 결과 삭제 여부 (append가 false면 기존 결과 삭제)
    if (!append) {
        resultsContainer.innerHTML = '';
        displayedCnt = 0;
    }

    console.log(data);

    if (data.success == false) {
        console.log(data.errorDto.errorField);
        console.log(data.errorDto.errorMessage);
        alert(data.errorDto.errorMessage)
        return;
    }

    // 검색 결과가 없을 경우 메시지 표시
    if (data.data.length === 0 && currentPage === 0) {
        const emptyMessage = document.createElement('tr');
        const td = document.createElement('td');
        td.setAttribute('colspan', 4); // 4열에 맞추기 위해
        td.textContent = "검색 결과가 없습니다.";
        emptyMessage.appendChild(td);
        resultsContainer.appendChild(emptyMessage);
        const resultsSummary = document.getElementById('resultsSummary');
        resultsSummary.textContent = '';
        return;
    }
    // 데이터를 순회하면서 결과 추가
    data.data.forEach(item => {
        const row = document.createElement('tr');

        // 각 열 데이터 추가
        const repoCell = document.createElement('td');
        repoCell.textContent = item.repositoryName;
        row.appendChild(repoCell);

        const fileCell = document.createElement('td');
        const repoName = item.repositoryName;
        //const path = item.filePath;
        // const startIndex = path.indexOf('gitProjects\\') + 'gitProjects\\'.length;
        const path = item.filePath.replace(/\\/g, '/');
        const repoIndex = path.indexOf('/' + repoName + '/');
        let pathTextContent = path;
        if (repoIndex >= 0) {
            pathTextContent = path.substring(repoIndex + 1); // '/' 제외하고 repoName부터 시작
        }

        //const pathTextContent = path.substring(startIndex);
        fileCell.textContent = pathTextContent;

        row.appendChild(fileCell);

        const lineNumCell = document.createElement('td');
        lineNumCell.textContent = item.lineNumber;
        row.appendChild(lineNumCell);

        const lineContentCell = document.createElement('td');
        lineContentCell.textContent = item.lineContent;
        row.appendChild(lineContentCell);

        // 클릭 시 다른 탭으로 이동
        const repositoryName = encodeURIComponent(item.repositoryName);
        const fileName = encodeURIComponent(item.fileName);
        const filePath = encodeURIComponent(item.filePath);
        const lineNumber = encodeURIComponent(item.lineNumber);
        row.addEventListener('click', () => {
            window.open(`/file/viewer?repositoryName=${repositoryName}&fileName=${fileName}&filePath=${filePath}&lineNumber=${lineNumber}`, '_blank');
        });

        resultsContainer.appendChild(row);
    });
    // doc id 셋팅
    document.getElementById('hiddenLastScoreDocId').value = data.docInfoDto.lastScoreDocId;
    document.getElementById('hiddenDocScore').value = data.docInfoDto.docScore;

    console.log("data create")

    /* TODO 건수 표시 다시 해보기
    // DOM 업데이트 이후에 카운트
    // setTimeout(() => {
        // 현재 표시된 결과 수 / 총 페이지 수를 표시할 요소
        const resultsSummary = document.getElementById('resultsSummary');

        // 총 결과 수와 현재 페이지 수를 표시 (totalCnt는 서버에서 받아온 값)
        totalCnt = data.totalCnt || 0;  // totalCnt 값을 받아옴 (기본값 0)
        displayedCnt = document.querySelectorAll('.search-results tr').length || 0;  // 현재 표시된 결과 수

        // 결과 수를 표시
        resultsSummary.textContent = `현재 ${displayedCnt}개 / 총 ${totalCnt}개`;
    // }, 3000);
     */
    const resultsSummary = document.getElementById('resultsSummary');
    if (!append) {
        totalCnt = data.totalCnt || 0;  // totalCnt 값을 받아옴 (기본값 0)
    }
    displayedCnt = document.querySelectorAll('.search-results tr').length || 0;  // 현재 표시된 결과 수
    resultsSummary.textContent = `현재 ${displayedCnt}개 / 총 ${totalCnt}개`;
}

// 모달 제어 함수
function showModal() {
    document.getElementById("loadingModal").style.display = "flex";
}

function hideModal() {
    document.getElementById("loadingModal").style.display = "none";
}

// 단위 검색 제어
function toggleResarchOption() {
    const searchType = document.getElementById("searchType").value;
    const reSearchOption = document.getElementById("reSearchOption");

    // 파일 단위 검색이면 재검색 체크박스를 숨기고, 행 단위 검색이면 보이게 함
    if (searchType === "file") {
        reSearchOption.style.display = "none"; // 체크박스를 숨김
    } else {
        reSearchOption.style.display = "flex"; // 체크박스를 다시 표시
    }
}

// 페이지가 로드될 때 현재 선택된 값에 따라 초기 상태 설정
document.addEventListener("DOMContentLoaded", function() {
    toggleResarchOption();  // 페이지 로드 시 실행하여 초기 상태를 설정
});

// 엔터키 감지
function triggerSearchOnEnter(event) {
    if (event.key === 'Enter' || event.keyCode === 13) {
        search();
    }
}

// 결과내 재검색 체크시 동작
function toggleExtensionOption() {
    const extensionOption = document.getElementById("extensionOption");
    const searchWithinResults = document.getElementById("searchWithinResults");

    if (searchWithinResults.checked) {
        extensionOption.style.removeProperty("display");
    } else {
        extensionOption.style.display = "none";
    }
}