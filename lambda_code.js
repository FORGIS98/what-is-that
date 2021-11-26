const langs = {
    "es": ["ejemplo"],
    "en": ["example"],
    "fr": ["exemple"],
    "de": ["voorbeeld"],
    "ja": ["例"]
    
};

exports.handler = async (event) => {
    let response;
    console.log("Input: " + event);
    
    // Validate the entry //
    if (event.body === null || event.body === undefined) {
        response = {
            statusCode: 400,
            body: JSON.stringify('Body not found!'),
        };
        
        return response;
    }
    
    if (event.body.word === null || event.body.word === undefined
        || event.body.lang === null || event.body.lang === undefined) {
        response = {
            statusCode: 400,
            body: JSON.stringify('Missing body elements!'),
        };
        
        return response;
    }
    
    // Get translated word if exists //
    if (langs["en"].includes(event.body.word.toLowerCase())) {
        let index = langs["en"].indexOf(event.body.word.toLowerCase());
        
        response = {
            statusCode: 200,
            body: JSON.stringify(langs[event.body.lang][index])
        };
    } else {
        response = {
            statusCode: 404,
            body: JSON.stringify('Word not found!'),
        };
    }
    
    return response;
};
