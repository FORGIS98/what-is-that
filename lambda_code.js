const langs = {
    "es": ["ejemplo"],
    "en": ["example"],
    "fr": ["exemple"],
};

exports.handler = async (event) => {
    
    let response;
    
    console.info("BODY:\n" + JSON.stringify(event.body, null, 2));
    
    event = JSON.parse(event.body);
    
    // Validate the entry //
    if (event === null || event === undefined) {
        response = {
            statusCode: 400,
            body: JSON.stringify('Body not found!'),
        };
        
        return response;
    }
    
    if (event.word === null || event.word === undefined
        || event.lang === null || event.lang === undefined) {
        response = {
            statusCode: 400,
            body: JSON.stringify('Missing body elements!'),
        };
        
        return response;
    }
    
    // Get translated word if exists //
    if (langs["en"].includes(event.word.toLowerCase())) {
        let index = langs["en"].indexOf(event.word.toLowerCase());
        
        response = {
            statusCode: 200,
            body: JSON.stringify(langs[event.lang][index])
        };
    } else {
        response = {
            statusCode: 404,
            body: JSON.stringify('Word not found!'),
        };
    }
    
    return response;
    
};
